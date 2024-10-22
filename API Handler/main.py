from datetime import datetime, timedelta
import time
from threading import Thread

from colorama import Fore

import Game
import Match
import RiotAPIUtils
import FirebaseMessageUtils
import FirebaseStorageUtils

esportsMatchesId = ['108998961199764690']
liveMatches = []
startedMatches = []


def startMatch(matchMetadata):

    print(matchMetadata)

    if FirebaseStorageUtils.matchIsScheduled(matchMetadata['id']):
        FirebaseStorageUtils.moveMatchFromScheduledToLive(matchMetadata['id'])
    else:
        FirebaseStorageUtils.addMatchToLive(matchMetadata)

    match = Match.Match(matchMetadata)

    match.startMatch()

    time.sleep(600)
    print("Live match completed!")
    FirebaseStorageUtils.moveMatchFromLiveToCompleted(matchMetadata['id'])

def addScheduledMatchesToDatabase(scheduledMatches):
    for match in scheduledMatches['data']['schedule']['events']:
        if match['state'] == 'unstarted' and "TFT" not in match['league']['name']:
            FirebaseStorageUtils.addMatchToScheduled(match)

addScheduledMatchesToDatabase(RiotAPIUtils.getScheduledEvents())

# for esportsMatchId in esportsMatchesId:
#     thread = Thread(target=startMatch, args=(RiotAPIUtils.getMatchMetadata(esportsMatchId),))
#     thread.start()

# FirebaseStorageUtils.moveMatchFromScheduledToLive('108998961199764690')
#
# FirebaseStorageUtils.moveMatchFromLiveToCompleted('108998961199764690')

# Main flow

while True:
    validResponse = False
    while not validResponse:
        try:
            liveEvents = RiotAPIUtils.getLiveEvents()
            validResponse = True
        except Exception as e:
            print(Fore.RED + str(e) + Fore.RESET)
            time.sleep(1)

    for event in liveEvents['data']['live']['events']:
        if event['state'] == 'inProgress' and not FirebaseStorageUtils.matchIsLive(event['id']) and not FirebaseStorageUtils.matchIsCompleted(event['id']):
            thread = Thread(target=startMatch, args=(event,))
            thread.start()
            print('Live match started!')

    time.sleep(120)
