import json
from datetime import datetime, timedelta
import time

from colorama import Fore

import RiotAPIUtils
import FirebaseMessageUtils
import FirebaseStorageUtils
import Game


class Match:
    def __init__(self, matchMetadata=None, matchObjectJson=None):
        if matchMetadata:
            self.esportsMatchId = matchMetadata['id']
            self.startTime = RiotAPIUtils.rfc460TimestampToDatetime(matchMetadata['startTime'])
            self.numberOfGames = matchMetadata['match']['strategy']['count']
            self.games = matchMetadata['games']
            self.teams = {}
            self.winner = None

            if self.numberOfGames == 1:
                self.necessaryWins = 1
            elif self.numberOfGames == 3:
                self.necessaryWins = 2
            elif self.numberOfGames == 5:
                self.necessaryWins = 3

            for i in range(0, len(matchMetadata['teams'])):
                self.teams[matchMetadata['teams'][i]['id']] = {}
                self.teams[matchMetadata['teams'][i]['id']]['name'] = FirebaseStorageUtils.getTeamDataById(matchMetadata['teams'][i]['id'])['name']
                self.teams[matchMetadata['teams'][i]['id']]['wins'] = 0

        if matchObjectJson:
            self.esportsMatchId = matchObjectJson['esportsMatchId']
            self.startTime = matchObjectJson['startTime']
            self.numberOfGames = matchObjectJson['numberOfGames']
            self.games = matchObjectJson['games']
            self.teams = matchObjectJson['teams']
            self.winner = matchObjectJson['winner']
            self.necessaryWins = matchObjectJson['necessaryWins']

    def startMatch(self):
        gameNumber = 1
        if self.necessaryWins > 1:
            self.sendStartingNotification()
        for game in self.games:
            self.startGame(game['id'], gameNumber)

            if not self.hasWinner():
                self.sendNotificationScore()

            gameNumber += 1

    def sendStartingNotification(self):
        if self.necessaryWins > 1:
            self.sendMatchNotification("Match has just started!")

    def getGamesPlayed(self):
        gamesPlayed = 0
        for teamId in self.teams.keys():
            gamesPlayed += self.teams[teamId]['wins']

        return gamesPlayed

    def hasWinner(self):
        for teamId in self.teams.keys():
            if self.teams[teamId]['wins'] == self.necessaryWins:
                if self.necessaryWins > 1:
                    notificationWinner = self.createNotificationScore()
                    notificationWinner += "\n" + self.teams[teamId]['name'] + " WINS THE MATCH!"
                    self.sendMatchNotification(notificationWinner)
                self.winner = self.teams[teamId]
                return True

        return False

    def sendNotificationScore(self):
        notificationScore = self.createNotificationScore()
        self.sendMatchNotification(notificationScore)

    def createNotificationScore(self):
        notificationScore = self.teams[list(self.teams.keys())[0]]['name'] + "  " + str(
            self.teams[list(self.teams.keys())[0]]['wins'])
        notificationScore += "-" + str(self.teams[list(self.teams.keys())[1]]['wins']) + "  " + \
                             self.teams[list(self.teams.keys())[1]]['name']

        return notificationScore

    def startGame(self, esportsGameId, gameNumber):
        time.sleep(10)

        gameStarted = False

        while not gameStarted:
            try:
                request = RiotAPIUtils.getEsportsGameSnapshots(esportsGameId)
                if request.json() and 'esportsGameId' in dict(request.json()).keys():
                    gameStarted = True
                print(request.json())

            except Exception as e:
                print(Fore.RED + str(e) + Fore.RESET)
                time.sleep(5)

        if len(self.games) == 1: gameNumber == 0

        game = Game.Game(self.esportsMatchId, request.json()['esportsGameId'], request.json()['frames'][0],
                         request.json()['gameMetadata'], gameNumber)

        date = request.json()['frames'][0]['rfc460Timestamp']

        date = RiotAPIUtils.roundDateForSnapshot(date)

        date = str(date).replace(' ', 'T')

        snapshotCount = 1

        pastSnapshot = request.json()

        while request.json()['frames'][-1]['gameState'] != 'finished':

            print("Snapshot number " + str(snapshotCount))
            print(date)
            print(request.json()['frames'][-1])

            requestSuccess = False
            while not requestSuccess:
                try:
                    request = RiotAPIUtils.getEsportsGameSnapshots(esportsGameId, date)
                    if 'frames' in dict(request.json()).keys():
                        requestSuccess = True
                except Exception as e:
                    print(Fore.RED + str(e) + Fore.RESET)
                    time.sleep(1)

            if pastSnapshot['frames'][-1] != request.json()['frames'][-1]:
                for i in range(0, len(request.json()['frames'])):
                    game.update(request.json()['frames'][i])

            dt_object = datetime.strptime(date, '%Y-%m-%dT%H:%M:%S')
            date = str(dt_object + timedelta(seconds=10))
            date = date.replace(" ", "T")

            pastSnapshot = request.json()

            time.sleep(10)
            snapshotCount += 1

        print(str(self.teams))
        print(self.teams.keys())
        print(game.winner)
        self.teams[game.winner]['wins'] += 1

    def addGameWinner(self, teamId):
        self.teams[teamId]['wins'] += 1

    def sendMatchNotification(self, notification):
        team1Name = self.teams[list(self.teams.keys())[0]]['name']
        team2Name = self.teams[list(self.teams.keys())[1]]['name']

        matchTitle = team1Name + " V/S " + team2Name

        print(matchTitle)
        print(notification)

        FirebaseMessageUtils.sendNotification(self.esportsMatchId, matchTitle, notification)
