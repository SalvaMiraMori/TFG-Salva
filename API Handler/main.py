from datetime import datetime, timedelta
import time
from threading import Thread

from colorama import Fore

import Game
import RiotAPIUtils

esportsGamesId = ['108998961199174707', '107417293272826598']
liveGames = []
startedGames = []


def startGame(esportsGameId):
    time.sleep(10)

    gameStarted = False

    while not gameStarted:
        try: ########
            request = RiotAPIUtils.getEsportsGameSnapshots(esportsGameId)
            if request.json() and 'esportsGameId' in dict(request.json()).keys():
                gameStarted = True
            print(request.json())

        except Exception as e:
            print(Fore.RED + str(e) + Fore.RESET)
            time.sleep(5)


    game = Game.Game(request.json()['esportsGameId'], request.json()['frames'][0], request.json()['gameMetadata'])
    print("GameId: " + game.esportsGameId)
    print("Game start time: " + str(game.startingTime))
    # print("Game state: " + str(game.gameState))

    date = request.json()['frames'][0]['rfc460Timestamp']
    # print(date)
    roundedSeconds = round(int(date[-7:-5]) + 5, -1)

    if roundedSeconds == 0:
        roundedSeconds = '00'

    date = date[:-7] + str(roundedSeconds)

    print(date)

    while request.json()['frames'][-1]['gameState'] != 'finished':

        requestSuccess = False
        while not requestSuccess:
            try:
                request = RiotAPIUtils.getEsportsGameSnapshots(esportsGameId, date)
                if 'frames' in dict(request.json()).keys():
                    requestSuccess = True
            except Exception as e:
                print(Fore.RED + str(e))
                time.sleep(1)
        # print(request.json())
        for i in range(0, len(request.json()['frames'])):
            game.update(request.json()['frames'][i])

        dt_object = datetime.strptime(date, '%Y-%m-%dT%H:%M:%S')
        date = str(dt_object + timedelta(seconds=10))
        date = date.replace(" ", "T")

        time.sleep(10)

    print('Game finished')
    print(request.json()['frames'][-1]['gameState'])
    # startedGames.remove(esportsGameId)

def removeGameFromLiveList(esportsGameId):
    startedGames.remove(esportsGameId)
    time.sleep(600)
    liveGames.remove(esportsGameId)
    print("Live game completed!")


# for esportsGameId in esportsGamesId:
#     thread = Thread(target=startGame, args=(esportsGameId,))
#     thread.start()

# Main flow
while True:
    # actualLiveGames = []

    validResponse = False
    while not validResponse:
        try:
            liveEvents = RiotAPIUtils.getLiveEvents()
            validResponse = True
        except Exception as e:
            print(Fore.RED + str(e))
            time.sleep(1)


    for event in liveEvents.json()['data']['live']['events']:
        for game in event['games']:
            if game['state'] == 'inProgress' and game['id'] not in startedGames:
                thread = Thread(target=startGame, args=(game['id'],))
                thread.start()
                print('Live game started!')
                startedGames.append(game['id'])
                if game['id'] not in liveGames:
                    liveGames.append(game['id'])

            elif game['state'] == 'unstarted' and game['id'] not in liveGames:
                print("Found a live game! Waiting for it to start.")
                liveGames.append(game['id'])


    if len(liveGames) == 0:
        print("Haven't found a live game yet!")
        time.sleep(30)
    else:
        time.sleep(120)
    # time.sleep(30)
