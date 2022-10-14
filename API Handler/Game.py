from datetime import datetime

import RiotAPIUtils
import FirebaseMessageUtils


class Game:
    def __init__(self, esportsGameId, gameState, gameMetadata):
        self.esportsGameId = esportsGameId
        self.gameState = dict(gameState)
        self.startingTime = RiotAPIUtils.rfc460TimestampToDatetime(gameState['rfc460Timestamp'])
        self.playingTime = RiotAPIUtils.rfc460TimestampToDatetime(gameState['rfc460Timestamp']) - RiotAPIUtils.rfc460TimestampToDatetime(gameState['rfc460Timestamp'])
        self.teams = {}
        self.teams['redTeam'] = Team(gameMetadata['redTeamMetadata'])
        self.teams['blueTeam'] = Team(gameMetadata['blueTeamMetadata'])
        self.firstBlood = False
        self.firstTower = False

    def update(self, newGameState):

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

        self.gameState = {k: v for k, v in newGameState.items()}
        self.playingTime = RiotAPIUtils.rfc460TimestampToDatetime(newGameState['rfc460Timestamp']) - self.startingTime

        self.createTeamNotifications(blueTeamDiff, 'blueTeam')
        self.createTeamNotifications(redTeamDiff, 'redTeam')

        self.createParticipantNotifications(blueParticipantsDiff, 'blueTeam')
        self.createParticipantNotifications(redParticipantsDiff, 'redTeam')

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
                multiKillValue = participant.multikill
                multiKillType = " kill"
                secSinceLastKill = 0

                if participant.lastKillDate:
                    secSinceLastKill = int(str(actualDate - participant.lastKillDate)[-2:]) + int(str(actualDate - participant.lastKillDate)[-5:-3]) * 60

                if multiKillValue == 0:
                    participant.updateMultikill(actualDate)
                elif multiKillValue == 1 and secSinceLastKill <= 10:
                    participant.updateMultikill(actualDate)
                    multiKillType = " double kill"
                elif multiKillValue == 2 and secSinceLastKill <= 10:
                    participant.updateMultikill(actualDate)
                    multiKillType = " triple kill"
                elif multiKillValue == 3 and secSinceLastKill <= 10:
                    participant.updateMultikill(actualDate)
                    multiKillType = " quadra kill"
                elif multiKillValue == 4 and secSinceLastKill <= 30:
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

                self.sendNotification(notification)

            # Reset killing spree if participant dies
            if participantDiff['deaths'] > 0:
                participant.resetKillingSpree()



    def createTeamNotifications(self, teamDiff, side):
        if teamDiff['inhibitors'] > 0: self.sendNotification(self.teams[side].name + " destroyed an inhibitor!")
        if teamDiff['towers'] > 0:
            if self.firstTower:
                self.sendNotification(self.teams[side].name + " destroyed the first tower!")
                self.firstTower = True
            else:
                self.sendNotification(self.teams[side].name + " destroyed a tower.")
        if teamDiff['barons'] > 0: self.sendNotification(self.teams[side].name + " killed Baron Nashor!")
        if len(teamDiff['dragons']) > 0:
            for dragon in teamDiff['dragons']:
                self.sendNotification(self.teams[side].name + " killed a " + dragon + " dragon.")
                if(len(self.gameState[side]['dragons']) == 4):
                    self.sendNotification(self.teams[side].name + " got the " + dragon + " soul!")

    def sendNotification(self, notification):
        notification += self.addTeamsStateToNotification()
        print(notification)
        formattedPlayingTime = str(self.playingTime)
        if str(self.playingTime)[0] == '0':
            formattedPlayingTime = str(self.playingTime)[2:]
        teamsVsFormat = self.teams['blueTeam'].name + " vs " + self.teams['redTeam'].name
        FirebaseMessageUtils.sendNotification(self.esportsGameId, teamsVsFormat + " / " + formattedPlayingTime, notification)

    def addTeamsStateToNotification(self):
        state = "\n\n  " + self.teams['blueTeam'].name + "   V/S   " + self.teams['redTeam'].name + "   "
        state += "\nK:  " + f"{self.gameState['blueTeam']['totalKills']:02}" + "   |   " + f"{self.gameState['redTeam']['totalKills']:02}" + "  :K"
        blueTeamGold = self.gameState['blueTeam']['totalGold'] / 1000
        redTeamGold = self.gameState['redTeam']['totalGold'] / 1000
        state += "\nG: " + f"{blueTeamGold:04.1f}" + "k | " + f"{redTeamGold:04.1f}" + "k :G"
        state += "\nT:  " + f"{self.gameState['blueTeam']['towers']:02}" + "   |   " + f"{self.gameState['redTeam']['towers']:02}" + "  :T"
        state += "\nD:  " + f"{len(self.gameState['blueTeam']['dragons']):02}" + "   |   " + f"{len(self.gameState['redTeam']['dragons']):02}" + "  :D"
        return state


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