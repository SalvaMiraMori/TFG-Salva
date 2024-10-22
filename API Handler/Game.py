from datetime import datetime, timedelta

import RiotAPIUtils
import FirebaseMessageUtils
import FirebaseStorageUtils


class Game:

    def __init__(self, *args):
        if len(args) == 5:
            self.cleanInit(args)
        else:
            self.existingObjectInit(args)

    def cleanInit(self, args):
        self.esportsGameId = args[1]
        self.esportsMatchId = args[0]
        self.gameState = dict(args[2])
        self.gameNumber = args[4]
        self.gameMetadata = args[3]
        self.startingTime = RiotAPIUtils.rfc460TimestampToDatetime(self.gameState['rfc460Timestamp'])
        self.playingTime = RiotAPIUtils.rfc460TimestampToDatetime(
            self.gameState['rfc460Timestamp']) - RiotAPIUtils.rfc460TimestampToDatetime(self.gameState['rfc460Timestamp'])
        self.pausedTime = self.playingTime
        print(type(self.startingTime), str(self.startingTime), type(self.playingTime), str(self.playingTime))
        self.teams = {}
        self.teams['redTeam'] = Team(self.gameMetadata['redTeamMetadata'])
        self.teams['blueTeam'] = Team(self.gameMetadata['blueTeamMetadata'])
        self.firstBlood = False
        self.firstTower = False
        self.isPaused = False
        strGameNumber = ""
        if self.gameNumber > 0: strGameNumber = str(self.gameNumber) + " "
        startingNotification = self.teams['blueTeam'].name + " vs " + self.teams[
            'redTeam'].name + " game " + strGameNumber + "has just started!"
        self.sendGameNotification(startingNotification)
        self.winner = None

    def existingObjectInit(self, args):
        gameObjectJson = args[0]

        self.esportsMatchId = gameObjectJson['esportsMatchId']
        self.esportsGameId = gameObjectJson['esportsGameId']
        self.gameMetadata = gameObjectJson['gameMetadata']
        self.gameState = gameObjectJson['gameState']
        self.gameNumber = gameObjectJson['gameNumber']
        self.winner = gameObjectJson['winner']
        self.startingTime = datetime.strptime(str(gameObjectJson['startingTime'])[:-6], '%Y-%m-%d %H:%M:%S.%f')
        print(str(gameObjectJson['playingTime']))
        self.playingTime = timedelta(hours=int(str(gameObjectJson['playingTime'])[12:13]),
                                     minutes=int(str(gameObjectJson['playingTime'])[15:16]),
                                     seconds=int(str(gameObjectJson['playingTime'])[18:19]))
        self.pausedTime = timedelta(hours=int(str(gameObjectJson['pausedTime'])[12:13]),
                                     minutes=int(str(gameObjectJson['pausedTime'])[15:16]),
                                     seconds=int(str(gameObjectJson['pausedTime'])[18:19]))
        print(type(self.startingTime), str(self.startingTime), type(self.playingTime), str(self.playingTime))
        self.firstTower = gameObjectJson['firstTower']
        self.firstBlood = gameObjectJson['firstBlood']
        self.isPaused = gameObjectJson['isPaused']
        self.teams = {}
        self.teams['redTeam'] = Team(self.gameMetadata['redTeamMetadata'])
        self.teams['blueTeam'] = Team(self.gameMetadata['blueTeamMetadata'])


    def toDict(self):

        try:
            playingTime = datetime.strptime(str(self.playingTime), '%H:%M:%S')
        except ValueError as e:
            playingTime = datetime.strptime(str(self.playingTime), '%H:%M:%S.%f')

        try:
            pausedTime = datetime.strptime(str(self.pausedTime), '%H:%M:%S')
        except ValueError as e:
            pausedTime = datetime.strptime(str(self.pausedTime), '%H:%M:%S.%f')

        return {
            'esportsGameId': self.esportsGameId,
            'esportsMatchId': self.esportsMatchId,
            'gameState': self.gameState,
            'gameNumber': self.gameNumber,
            'startingTime': datetime.strptime(str(self.startingTime), '%Y-%m-%d %H:%M:%S.%f'),
            'playingTime': playingTime,
            'pausedTime': pausedTime,
            'firstBlood': self.firstBlood,
            'firstTower': self.firstTower,
            'isPaused': self.isPaused,
            'winner': self.winner,
            'gameMetadata': self.gameMetadata
        }

    def update(self, newGameState):
        if newGameState['gameState'] == 'in_game':

            blueParticipantsDiff = RiotAPIUtils.diffParticipants(newGameState['blueTeam']['participants'],
                                                                 self.gameState['blueTeam']['participants'])
            redParticipantsDiff = RiotAPIUtils.diffParticipants(newGameState['redTeam']['participants'],
                                                                self.gameState['redTeam']['participants'])

            newGameStateBlueTeam = {k: v for k, v in newGameState['blueTeam'].items() if k != 'participants'}
            newGameStateRedTeam = {k: v for k, v in newGameState['redTeam'].items() if k != 'participants'}
            gameStateBlueTeam = {k: v for k, v in self.gameState['blueTeam'].items() if k != 'participants'}
            gameStateRedTeam = {k: v for k, v in self.gameState['redTeam'].items() if k != 'participants'}

            blueTeamDiff = RiotAPIUtils.diffTeams(newGameStateBlueTeam, gameStateBlueTeam)
            redTeamDiff = RiotAPIUtils.diffTeams(newGameStateRedTeam, gameStateRedTeam)

            if self.isPaused:
                self.isPaused = False
                self.pausedTime = self.pausedTime + RiotAPIUtils.rfc460TimestampToDatetime(newGameState['rfc460Timestamp']) - RiotAPIUtils.rfc460TimestampToDatetime(self.gameState['rfc460Timestamp'])

            self.playingTime = RiotAPIUtils.rfc460TimestampToDatetime(newGameState['rfc460Timestamp']) - self.startingTime - self.pausedTime

            self.gameState = {k: v for k, v in newGameState.items()}

            self.createTeamNotifications(blueTeamDiff, 'blueTeam')
            self.createTeamNotifications(redTeamDiff, 'redTeam')

            self.createParticipantNotifications(blueParticipantsDiff, 'blueTeam')
            self.createParticipantNotifications(redParticipantsDiff, 'redTeam')

        elif newGameState['gameState'] == 'paused':
            self.isPaused = True
            self.gameState = {k: v for k, v in newGameState.items()}

        elif newGameState['gameState'] == 'finished':
            self.finishGame()
            self.gameState = {k: v for k, v in newGameState.items()}


    def createParticipantNotifications(self, participantsDiff, side):
        actualDate = RiotAPIUtils.rfc460TimestampToDatetime(self.gameState['rfc460Timestamp'])

        for participantDiff in participantsDiff:
            participant = self.teams[side].participants[participantDiff['participantId']]

            if participantDiff['kills'] > 0:
                notification = ""

                # Check if first blood
                if not self.firstBlood:
                    notification += participant.summonerName + " got the first blood!\n"
                    self.firstBlood = True

                # Check multikill
                multiKillValue = participant.multikill + participantDiff['kills']
                multiKillType = " kill"
                secSinceLastKill = 0

                if participant.lastKillDate:
                    secSinceLastKill = int(str(actualDate - participant.lastKillDate)[-2:]) + int(
                        str(actualDate - participant.lastKillDate)[-5:-3]) * 60

                if multiKillValue == 1:
                    participant.updateMultikill(actualDate)
                elif multiKillValue == 2 and secSinceLastKill <= 10:
                    participant.updateMultikill(actualDate)
                    multiKillType = " double kill"
                elif multiKillValue == 3 and secSinceLastKill <= 10:
                    participant.updateMultikill(actualDate)
                    multiKillType = " triple kill"
                elif multiKillValue == 4 and secSinceLastKill <= 10:
                    participant.updateMultikill(actualDate)
                    multiKillType = " quadra kill"
                elif multiKillValue == 5 and secSinceLastKill <= 30:
                    participant.updateMultikill(actualDate)
                    multiKillType = " PENTAKILL"
                else:
                    participant.resetMultikill()

                notification += participant.summonerName + " got a" + multiKillType + "."

                # Check killing spree
                participant.killingSpree += 1

                if participant.killingSpree == 3:
                    notification += "\n" + participant.summonerName + " is on a killing spree!"
                elif participant.killingSpree == 4:
                    notification += "\n" + participant.summonerName + " is on a rampage!"
                elif participant.killingSpree == 5:
                    notification += "\n" + participant.summonerName + " is unstoppable!"
                elif participant.killingSpree == 6:
                    notification += "\n" + participant.summonerName + " is dominating!"
                elif participant.killingSpree == 7:
                    notification += "\n" + participant.summonerName + " is godlike!"
                elif participant.killingSpree >= 8:
                    notification += "\n" + participant.summonerName + " is legendary!"

                self.sendGameNotification(notification)

            # Reset killing spree if participant dies
            if participantDiff['deaths'] > 0:
                participant.resetKillingSpree()

    def createTeamNotifications(self, teamDiff, side):
        if teamDiff['inhibitors'] > 0: self.sendGameNotification(self.teams[side].name + " destroyed an inhibitor!")
        if teamDiff['towers'] > 0:
            if not self.firstTower:
                self.sendGameNotification(self.teams[side].name + " destroyed the first tower!")
                self.firstTower = True
            else:
                self.sendGameNotification(self.teams[side].name + " destroyed a tower.")
        if teamDiff['barons'] > 0: self.sendGameNotification(self.teams[side].name + " killed Baron Nashor!")
        if teamDiff['dragon']:
            self.sendGameNotification(self.teams[side].name + " killed a " + teamDiff['dragon'] + " dragon.")
            if (len(self.gameState[side]['dragons']) == 4):
                self.sendGameNotification(self.teams[side].name + " got the " + teamDiff['dragon'] + " soul!")

    def sendGameNotification(self, notification):
        notification += self.addTeamsStateToNotification()
        print(notification)
        formattedPlayingTime = str(self.playingTime)[:-7]
        if str(self.playingTime)[0] == '0':
            formattedPlayingTime = formattedPlayingTime[2:]
        if str(self.playingTime) == "0:00:00":
            formattedPlayingTime = '00:00'
        print(self.playingTime, formattedPlayingTime, str(self.startingTime), str(self.pausedTime))
        teamsVsFormat = self.teams['blueTeam'].name + " vs " + self.teams['redTeam'].name
        FirebaseStorageUtils.addNotificationToGame(self.esportsMatchId, self.esportsGameId, notification,
                                                   formattedPlayingTime, self.teams['blueTeam'].name, self.teams['redTeam'].name)
        FirebaseMessageUtils.sendNotification(self.esportsMatchId, teamsVsFormat + " / " + formattedPlayingTime,
                                              notification)

    def addTeamsStateToNotification(self):
        state = "\n\n  " + self.teams['blueTeam'].name + "   V/S   " + self.teams['redTeam'].name + "   "
        state += "\nK:  " + f"{self.getTeamTotalKills('blueTeam'):02}" + "   |   " + f"{self.getTeamTotalKills('redTeam'):02}" + "  :K"
        blueTeamGold = self.gameState['blueTeam']['totalGold'] / 1000
        redTeamGold = self.gameState['redTeam']['totalGold'] / 1000
        state += "\nG: " + f"{blueTeamGold:04.1f}" + "k | " + f"{redTeamGold:04.1f}" + "k :G"
        state += "\nT:  " + f"{self.gameState['blueTeam']['towers']:02}" + "   |   " + f"{self.gameState['redTeam']['towers']:02}" + "  :T"
        state += "\nD:  " + f"{len(self.gameState['blueTeam']['dragons']):02}" + "   |   " + f"{len(self.gameState['redTeam']['dragons']):02}" + "  :D"
        return state

    def getTeamById(self, esportsTeamId):
        for side in self.teams.keys():
            if self.teams[side].esportsTeamId == esportsTeamId:
                return self.teams[side]

    def getTeamTotalKills(self, sideTeam):
        return sum([participant['kills'] for participant in self.gameState[sideTeam]['participants']])

    def finishGame(self):
        gameTeamIdWinner = RiotAPIUtils.getGameSideWinner(self.esportsGameId)
        self.winner = gameTeamIdWinner
        gameWinnerNotification = self.getTeamById(gameTeamIdWinner).name + " wins the game!"
        self.sendGameNotification(gameWinnerNotification)


class Team:
    def __init__(self, teamMetadata):
        self.esportsTeamId = teamMetadata['esportsTeamId']
        self.participants = {}
        self.name = teamMetadata['participantMetadata'][0]['summonerName'].split()[0]
        for participantMetadata in teamMetadata['participantMetadata']:
            self.participants[participantMetadata['participantId']] = Participant(participantMetadata)


class Participant:
    def __init__(self, participantMetadata):
        self.participantId = participantMetadata['participantId']
        self.esportsPlayerId = participantMetadata['esportsPlayerId']
        self.summonerName = participantMetadata['summonerName']
        self.championId = participantMetadata['championId']
        self.role = participantMetadata['role']
        self.lastKillDate = None
        self.multikill = 0
        self.killingSpree = 0

    def updateMultikill(self, date):
        self.multikill += 1
        self.lastKillDate = date

    def resetMultikill(self):
        self.multikill = 0

    def resetKillingSpree(self):
        self.killingSpree = 0
