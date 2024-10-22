from collections import Counter
from datetime import datetime, timedelta
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
    # diffDict['dragons'] = [dragon for dragon in dragons if dragon not in prevDragons]
    if len(dragons) > len(prevDragons): diffDict['dragon'] = dragons[-1]
    else: diffDict['dragon'] = None

    return diffDict


apikey = 'DSyqfSI1HM7uylEKOmsdN2Vw0Kl0lIIy2IHSTATz'

headers = {'x-api-key': apikey}


def getEsportsGameSnapshots(esportsGameId, date=None):
    request = {}
    connectionError = True
    while connectionError:
        try:
            if date:
                request = requests.get(
                    'https://feed.lolesports.com/livestats/v1/window/' + esportsGameId + '?startingTime=' + date + '.000Z')
            else:
                request = requests.get('https://feed.lolesports.com/livestats/v1/window/' + esportsGameId)
            connectionError = False
        except Exception as e:
            print(Fore.RED + str(e) + Fore.RESET)
            time.sleep(1)

    return request


def getScheduledEvents(page = None):

    request = {}
    connectionError = True
    while connectionError:
        try:
            if page:
                request = requests.get('https://esports-api.lolesports.com/persisted/all/schedule?hl=en-US&pageToken=' + page, headers=headers).json()
            else:
                request = requests.get('https://esports-api.lolesports.com/persisted/all/schedule?hl=en-US', headers=headers).json()
            connectionError = False
        except Exception as e:
            print(Fore.RED + str(e) + Fore.RESET)
            time.sleep(1)

    return request


def getLiveEvents():
    request = {}
    connectionError = True
    while connectionError:
        try:
            request = requests.get("https://esports-api.lolesports.com/persisted/all/live?hl=en-US", headers=headers).json()
            connectionError = False
        except Exception as e:
            print(Fore.RED + str(e) + Fore.RESET)
            time.sleep(1)

    return request


def rfc460TimestampToDatetime(date):
    if len(date) == 24:
        return datetime.strptime(date[:-1] + '000', '%Y-%m-%dT%H:%M:%S.%f')
    else:
        return datetime.strptime(date[:-2] + '.000000', '%Y-%m-%dT%H:%M:%S.%f')


def getGameSideWinner(esportsGameId):
    while True:
        scheduledEvents = getScheduledEvents()

        for data in scheduledEvents['data']['schedule']['events']:
            for game in data['games']:
                if game['id'] == esportsGameId:
                    if game['state'] == 'completed':
                        for team in game['teams']:
                            if team['result']['outcome'] == 'win':
                                return team['id']
                    else:
                        time.sleep(1)


def getMatchMetadata(esportsMatchId, page = None):
    scheduledEvents = getScheduledEvents(page)
    # print(scheduledEvents)
    for event in scheduledEvents['data']['schedule']['events']:
        # print(event['id'])
        if event['id'] == esportsMatchId:
            print(event['id'])
            return event

    return None

def roundDateForSnapshot(date):
    date = rfc460TimestampToDatetime(date)
    date = round_time(dt=date, date_delta=timedelta(seconds=10), to='down')

    date = date + timedelta(seconds=10)

    return date


def round_time(dt=None, date_delta=timedelta(minutes=1), to='average'):
    """
    Round a datetime object to a multiple of a timedelta
    dt : datetime.datetime object, default now.
    dateDelta : timedelta object, we round to a multiple of this, default 1 minute.
    from:  http://stackoverflow.com/questions/3463930/how-to-round-the-minute-of-a-datetime-object-python
    """
    round_to = date_delta.total_seconds()
    if dt is None:
        dt = datetime.now()
    seconds = (dt - dt.min).seconds

    if seconds % round_to == 0 and dt.microsecond == 0:
        rounding = (seconds + round_to / 2) // round_to * round_to
    else:
        if to == 'up':
            # // is a floor division, not a comment on following line (like in javascript):
            rounding = (seconds + dt.microsecond/1000000 + round_to) // round_to * round_to
        elif to == 'down':
            rounding = seconds // round_to * round_to
        else:
            rounding = (seconds + round_to / 2) // round_to * round_to

    return dt + timedelta(0, rounding - seconds, - dt.microsecond)
