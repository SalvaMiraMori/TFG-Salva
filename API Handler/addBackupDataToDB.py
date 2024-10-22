import FirebaseStorageUtils
import RiotAPIUtils
from datetime import datetime, timedelta
from colorama import Fore
import time

esportsMatchId = '107458367241215655'
page = 'bmV3ZXI6OjEwNzQxNzc0MTE5NDA4NTYwMg=='

matchMetadata = RiotAPIUtils.getMatchMetadata(esportsMatchId, page)

print(matchMetadata)

FirebaseStorageUtils.addBackupMatchMetadata(matchMetadata)

for game in matchMetadata['games']:

    request = RiotAPIUtils.getEsportsGameSnapshots(game['id'])

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
                request = RiotAPIUtils.getEsportsGameSnapshots(game['id'], date)
                if 'frames' in dict(request.json()).keys():
                    requestSuccess = True
            except Exception as e:
                print(Fore.RED + str(e) + Fore.RESET)
                time.sleep(1)

        FirebaseStorageUtils.addBackupGameSnapshot(request.json(), snapshotCount)

        dt_object = datetime.strptime(date, '%Y-%m-%dT%H:%M:%S')
        date = str(dt_object + timedelta(seconds=10))
        date = date.replace(" ", "T")

        if pastSnapshot == request.json():
            break

        time.sleep(10)
        snapshotCount += 1