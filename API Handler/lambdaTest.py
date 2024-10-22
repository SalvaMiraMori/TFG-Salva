import json
from datetime import datetime, timedelta

import pytz
from dateutil.tz import tzutc

import FirebaseStorageUtils
import FirebaseMessageUtils
import RiotAPIUtils
import Game
import Match
import time
import asyncio


def GetScheduledMatches():
    validResponse = False
    while not validResponse:
        try:
            scheduledEvents = RiotAPIUtils.getScheduledEvents()
            validResponse = True
        except Exception as e:
            print(str(e))
            time.sleep(1)

    for event in scheduledEvents['data']['schedule']['events']:
        if event['league']['name'] in ['LCK', 'LPL', 'LCS', 'LEC'] and event['state'] == 'unstarted':
            if not FirebaseStorageUtils.matchIsLive(event['id']) and not FirebaseStorageUtils.matchIsCompleted(event['id']) and not FirebaseStorageUtils.matchIsScheduled(event['id']):
                cet_date = RiotAPIUtils.rfc460TimestampToDatetime(event['startTime']).astimezone(pytz.timezone('Europe/Berlin'))
                print(cet_date.time())
                print(cet_date.date())
                print(cet_date.timestamp())
                print(cet_date.isoformat(timespec='seconds'))
                print(RiotAPIUtils.rfc460TimestampToDatetime(event['startTime']))
                if str(cet_date.isoformat(timespec='seconds'))[-6] == "+":
                    date = RiotAPIUtils.rfc460TimestampToDatetime(event['startTime']) + timedelta(hours=int(str(cet_date.isoformat(timespec='seconds'))[-4]))
                else:
                    date = RiotAPIUtils.rfc460TimestampToDatetime(event['startTime']) - timedelta(hours=int(str(cet_date.isoformat(timespec='seconds'))[-4]))

                date = date - timedelta(hours=1)

                print("cron(" + str(date.minute) + " " + str(date.hour) + " " + str(date.day) + " " + str(date.month) + " * " + str(date.year) + ")")

                FirebaseStorageUtils.addMatchToScheduled(event)

            # Schedule rule for CreateRuleMatchIsLive
            # CreateRuleMatchIsLive({'matchId': event['id']})

def CreateRuleMatchIsLive(event):
    # Create rule MatchIsLive every 2 minutes
    print('CreateRuleMatchIsLive')
    MatchIsLive(event)

def MatchIsLive(event):
    print('MatchIsLive')

    esportsMatchId = event['matchId']

    validResponse = False
    while not validResponse:
        try:
            liveEvents = RiotAPIUtils.getLiveEvents()
            validResponse = True
        except Exception as e:
            print(str(e))
            time.sleep(1)

    for event in liveEvents['data']['live']['events']:
        if event['state'] == 'inProgress' and event['id'] == esportsMatchId:
            FirebaseStorageUtils.addMatchToLive(event)
            match = Match.Match(matchMetadata=event)
            FirebaseStorageUtils.addMatchObjectJson(esportsMatchId, match.__dict__)
            # Delete rule MatchIsLive
            # Invoke lambda update match state
            UpdateMatchState({'matchId': esportsMatchId})

def UpdateMatchState(event):
    esportsMatchId = event['matchId']
    print(esportsMatchId)

    gamePlayedId = None

    try:
        gamePlayedId = event['gameId']
    except Exception as e:
        pass

    validResponse = False
    while not validResponse:
        try:
            liveMatchData = FirebaseStorageUtils.getLiveMatchData(esportsMatchId)
            validResponse = True
        except Exception as e:
            print(str(e))
            time.sleep(1)

    esportsGameId = None

    match = Match.Match(matchObjectJson=liveMatchData['matchObjectJson'])

    if gamePlayedId:
        match.addGameWinner(FirebaseStorageUtils.getLiveGameData(esportsMatchId, gamePlayedId)['gameObjectJson']['winner'])


    if not match.hasWinner():
        if match.getGamesPlayed() == 0:
            match.sendStartingNotification()
        else:
            match.sendNotificationScore()
        esportsGameId = match.games[match.getGamesPlayed()]['id']
    else:
        # Delete any rules
        pass

    FirebaseStorageUtils.addMatchObjectJson(esportsMatchId, match.__dict__)

    if esportsGameId:
        # Invoke lambda GameIsLive
        GameIsLive({'matchId': esportsMatchId, 'gameId': esportsGameId})

def GameIsLive(event):
    print('GameIsLive')
    esportsMatchId = event['matchId']
    esportsGameId = event['gameId']

    print(esportsMatchId, esportsGameId)

    gameStarted = False

    validResponse = False
    while not validResponse:
        try:
            request = RiotAPIUtils.getEsportsGameSnapshots(esportsGameId)
            validResponse = True
        except Exception as e:
            print(str(e))
            time.sleep(1)

    if request.json() and 'esportsGameId' in dict(request.json()).keys():
        gameStarted = True

    if gameStarted:
        date = request.json()['frames'][0]['rfc460Timestamp']
        date = RiotAPIUtils.roundDateForSnapshot(date)
        date = str(date).replace(' ', 'T')
        match = Match.Match(matchObjectJson=FirebaseStorageUtils.getLiveMatchData(esportsMatchId)['matchObjectJson'])
        game = Game.Game(esportsMatchId, request.json()['esportsGameId'], request.json()['frames'][0],
                         request.json()['gameMetadata'], match.getGamesPlayed() + 1)
        FirebaseStorageUtils.addGameObjectJson(esportsMatchId, esportsGameId, game.toDict())
        # Delete rule GameIsLive
        # Create rule UpdateGameState
        UpdateGameState({'matchId': esportsMatchId, 'gameId': esportsGameId, 'date': date})


def UpdateGameState(event):
    esportsMatchId = event['matchId']
    esportsGameId = event['gameId']
    date = event['date']

    game = Game.Game(FirebaseStorageUtils.getLiveGameData(esportsMatchId, esportsGameId)['gameObjectJson'])

    print(game.gameState)

    for i in range(6):
        if game.winner:
            break
        requestSuccess = False
        while not requestSuccess:
            try:
                request = RiotAPIUtils.getEsportsGameSnapshots(esportsGameId, date)
                if 'frames' in dict(request.json()).keys():
                    requestSuccess = True
            except Exception as e:
                print(str(e))
                time.sleep(1)

        print(request.json()['frames'][-1])

        if game.gameState != request.json()['frames'][-1]:
            for i in range(0, len(request.json()['frames'])):
                game.update(request.json()['frames'][i])
                if game.winner:
                    break

        dt_object = datetime.strptime(date, '%Y-%m-%dT%H:%M:%S')
        date = str(dt_object + timedelta(seconds=10))
        date = date.replace(" ", "T")

        # time.sleep(1)

    FirebaseStorageUtils.addGameObjectJson(esportsMatchId, esportsGameId, game.toDict())

    if game.winner:
        # Delete UpdateGameState rule
        UpdateMatchState({'matchId': esportsMatchId, 'gameId': esportsGameId})
    else:
        UpdateGameState({'matchId': esportsMatchId, 'gameId': esportsGameId, 'date': date})


# esportsMatchId = '108998961199764690'
#
# CreateRuleMatchIsLive({'matchId': esportsMatchId})
#
# for event in RiotAPIUtils.getScheduledEvents()['data']['schedule']['events']:
#     if event['id'] == esportsMatchId:
#         FirebaseStorageUtils.addMatchToLive(event)
#         match = Match.Match(matchMetadata=event)
#         FirebaseStorageUtils.addMatchObjectJson(esportsMatchId, match.__dict__)
#         UpdateMatchState({'matchId': esportsMatchId})


# loop = asyncio.get_event_loop()
# loop.run_until_complete()
# loop.close()

ids = ['109517090067719752', '109517090067719768', '109517090067719784', '109517090067719810', '109625152897595402', '109625152897661002']

for id in ids:
    FirebaseStorageUtils.moveMatchFromLiveToCompleted(id)
