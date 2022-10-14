from collections import Counter
from datetime import datetime
import time

import requests
from colorama import Fore


def diffParticipants(frame, prevFrame):
    diffFrame = []
    for i in range(0, len(frame)):
        participant = frame[i].copy()
        prevParticipant = prevFrame[i].copy()
        auxDict = {'participantId': participant.pop('participantId')}
        prevParticipant.pop('participantId')

        aux = Counter(participant)
        prevAux = Counter(prevParticipant)
        aux.subtract(prevAux)

        auxDict.update(dict(aux))
        diffFrame.append(auxDict)

    return diffFrame

def diffTeams(frame, prevFrame):
    frameD = frame.copy()
    prevFrameD = prevFrame.copy()
    dragons = frameD.pop('dragons')
    prevDragons = prevFrameD.pop('dragons')

    aux = Counter(frameD)
    prevAux = Counter(prevFrameD)
    aux.subtract(prevAux)

    diffDict = dict(aux)
    diffDict['dragons'] = [dragon for dragon in dragons if dragon not in prevDragons]

    return diffDict

apikey = 'DSyqfSI1HM7uylEKOmsdN2Vw0Kl0lIIy2IHSTATz'

headers = { 'x-api-key' : apikey }

def getEsportsGameSnapshots(esportsGameId, date = None):
    request = {}
    connectionError = True
    while connectionError:
        try:
            if date: request = requests.get('https://feed.lolesports.com/livestats/v1/window/' + esportsGameId + '?startingTime=' + date + '.000Z')
            else: request = requests.get('https://feed.lolesports.com/livestats/v1/window/' + esportsGameId)
            connectionError = False
        except Exception as e:
            print(Fore.RED + str(e))
            time.sleep(1)

    return request

def getScheduledEvents():
    return requests.get('https://esports-api.lolesports.com/persisted/all/schedule?hl=en-US', headers=headers)

def getLiveEvents():
    return requests.get("https://esports-api.lolesports.com/persisted/all/live?hl=en-US", headers=headers)

def getTeamData(teamId):
    pass

def rfc460TimestampToDatetime(date):
    if len(date) == 24:
        return datetime.strptime(date[:-5], '%Y-%m-%dT%H:%M:%S')
    else:
        return datetime.strptime(date[:-2], '%Y-%m-%dT%H:%M:%S')