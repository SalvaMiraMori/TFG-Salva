from datetime import datetime, timedelta
import time
from threading import Thread

from colorama import Fore
from tqdm import tqdm

import Game
import Match
import RiotAPIUtils
import FirebaseMessageUtils
import FirebaseStorageUtils

esportsMatchesId = ['108176672286663778']
# pages = ['b2xkZXI6OjEwNTY2MzUzOTE3NTQwMTcyMw==']
esportsMatchesId = ['108998961199371372', '108998961199371374', '108998961199371376', '108998961199436914', '108998961199436916', '108998961199436918', '108998961199436920']
liveMatches = []
startedMatches = []


# def startMatch(matchMetadata):
#
#     print(matchMetadata)
#
#     # if FirebaseStorageUtils.matchIsScheduled(matchMetadata['id']):
#     #     FirebaseStorageUtils.moveMatchFromScheduledToLive(matchMetadata['id'])
#     # else:
#     #     FirebaseStorageUtils.addMatchToLive(matchMetadata)
#     #
#     # match = Match.Match(matchMetadata)
#     #
#     # match.startMatch()
#     #
#     # print("Live match completed!")
#     # FirebaseStorageUtils.moveMatchFromLiveToCompleted(matchMetadata['id'])
#
# def addScheduledMatchesToDatabase(scheduledMatches):
#     for match in scheduledMatches['data']['schedule']['events']:
#         if match['state'] == 'unstarted' and "TFT" not in match['league']['name']:
#             FirebaseStorageUtils.addMatchToScheduled(match)
#
# for esportsMatchId in esportsMatchesId:
#     startMatch(RiotAPIUtils.getMatchMetadata(esportsMatchId))

