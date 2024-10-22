import time

import firebase_admin
from colorama import Fore
from firebase_admin import firestore, credentials


def getFirestoreClient():
    if not firebase_admin._apps:
        firebase_admin.initialize_app(credential=credentials.Certificate('secret_key.json'))

    return firestore.client()


def getTeamDataById(teamId):
    db = getFirestoreClient()

    doc_ref = db.collection(u'teams').document(teamId)

    doc = doc_ref.get()

    if doc.exists:
        return doc.to_dict()
    else:
        return None

def getLiveMatchData(esportsMatchId):
    db = getFirestoreClient()

    doc_ref = db.collection(u'LiveMatches').document(esportsMatchId)

    doc = doc_ref.get()

    if doc.exists:
        return doc.to_dict()
    else:
        return None

def getLiveGameData(esportsMatchId, esportsGameId):
    db = getFirestoreClient()

    doc_ref = db.collection(u'LiveMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId)

    doc = doc_ref.get()

    if doc.exists:
        return doc.to_dict()
    else:
        return None

def addMatchObjectJson(esportsMatchId, matchObjectJson):
    db = getFirestoreClient()

    data = {
        u'matchObjectJson': matchObjectJson
    }

    db.collection('LiveMatches').document(esportsMatchId).update(data)

def addGameObjectJson(esportsMatchId, esportsGameId, gameObjectJson):
    db = getFirestoreClient()

    data = {
        u'gameObjectJson': gameObjectJson
    }

    db.collection('LiveMatches').document(esportsMatchId).collection('Games').document(esportsGameId).update(data)


def addMatchToScheduled(matchMetadata):
    db = getFirestoreClient()

    for i in range(len(matchMetadata['teams'])):
        teamData = getTeamDataById(matchMetadata['teams'][i]['id'])
        if teamData:
            teamData.pop('id')
            matchMetadata['teams'][i] = {**matchMetadata['teams'][i], **teamData}

    data = {
        u'esportsMatchId': matchMetadata['id'],
        u'startTime': matchMetadata['startTime'],
        u'numberOfGames': matchMetadata['match']['strategy']['count'],
        u'teams': matchMetadata['teams']
    }

    db.collection(u'ScheduledMatches').document(matchMetadata['id']).set(data)

    for game in matchMetadata['games']:
        data = {
            u'esportsGameId': game['id'],
            u'state': game['state'],
            u'number': game['number'],
            u'teams': game['teams']
        }

        db.collection(u'ScheduledMatches').document(matchMetadata['id']).collection(u'Games').document(game['id']).set(data)


def addMatchToLive(matchMetadata):
    db = getFirestoreClient()

    for i in range(len(matchMetadata['teams'])):
        teamData = getTeamDataById(matchMetadata['teams'][i]['id'])
        if teamData:
            teamData.pop('id')
            matchMetadata['teams'][i] = {**matchMetadata['teams'][i], **teamData}

    data = {
        u'esportsMatchId': matchMetadata['id'],
        u'startTime': matchMetadata['startTime'],
        u'numberOfGames': matchMetadata['match']['strategy']['count'],
        u'teams': matchMetadata['teams']
    }

    db.collection(u'LiveMatches').document(matchMetadata['id']).set(data)

    for game in matchMetadata['games']:
        data = {
            u'esportsGameId': game['id'],
            u'state': game['state'],
            u'number': game['number'],
            u'teams': game['teams']
        }

        db.collection(u'LiveMatches').document(matchMetadata['id']).collection(u'Games').document(game['id']).set(
            data)


def addMatchToCompleted(matchMetadata):
    db = getFirestoreClient()

    for i in range(len(matchMetadata['teams'])):
        teamData = getTeamDataById(matchMetadata['teams'][i]['id'])
        if teamData:
            teamData.pop('id')
            matchMetadata['teams'][i] = {**matchMetadata['teams'][i], **teamData}

    data = {
        u'esportsMatchId': matchMetadata['id'],
        u'startTime': matchMetadata['startTime'],
        u'numberOfGames': matchMetadata['match']['strategy']['count'],
        u'teams': matchMetadata['teams']
    }

    db.collection(u'CompletedMatches').document(matchMetadata['id']).set(data)

    for game in matchMetadata['games']:
        data = {
            u'esportsGameId': game['id'],
            u'state': game['state'],
            u'number': game['number'],
            u'teams': game['teams']
        }

        db.collection(u'CompletedMatches').document(matchMetadata['id']).collection(u'Games').document(game['id']).set(
            data)


async def moveMatchFromScheduledToLive(esportsMatchId):
    db = getFirestoreClient()

    # Copy match metadata to LiveMatches
    data_ref = db.collection(u'ScheduledMatches').document(esportsMatchId)
    doc = data_ref.get()
    db.collection(u'LiveMatches').document(esportsMatchId).set(doc.to_dict())

    # Get Games collection reference from match in ScheduledMatches
    data_ref = db.collection(u'ScheduledMatches').document(esportsMatchId).collection(u'Games')
    docList = data_ref.get()

    for doc in docList:
        # Copy game metadata to match in LiveMatches
        esportsGameId = doc.id
        data_ref = db.collection(u'ScheduledMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId)
        doc = data_ref.get()
        db.collection(u'LiveMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId).set(doc.to_dict())

        # Delete game metadata from ScheduledMatches
        db.collection(u'ScheduledMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId).delete()

    # Delete match metadata from ScheduledMatches
    db.collection(u'ScheduledMatches').document(esportsMatchId).delete()


def moveMatchFromLiveToCompleted(esportsMatchId):
    db = getFirestoreClient()

    # Copy match metadata to LiveMatches
    data_ref = db.collection(u'LiveMatches').document(esportsMatchId)
    doc = data_ref.get()
    db.collection(u'CompletedMatches').document(esportsMatchId).set(doc.to_dict())

    while len(db.collection(u'CompletedMatches').document(esportsMatchId).get().to_dict()) == 0:
        print("Match was empty in Firebase, retrying.")
        db.collection(u'CompletedMatches').document(esportsMatchId).set(doc.to_dict())

    # Get Games collection reference from match in ScheduledMatches
    data_ref = db.collection(u'LiveMatches').document(esportsMatchId).collection(u'Games')
    gamesDocList = data_ref.get()

    for gamesDoc in gamesDocList:
        # Copy game metadata to match in LiveMatches
        esportsGameId = gamesDoc.id
        data_ref = db.collection(u'LiveMatches').document(esportsMatchId)\
            .collection(u'Games').document(esportsGameId)
        doc = data_ref.get()
        db.collection(u'CompletedMatches').document(esportsMatchId)\
            .collection(u'Games').document(esportsGameId).set(doc.to_dict())

        while len(db.collection(u'CompletedMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId).get().to_dict()) == 0:
            print("Match was empty in Firebase, retrying.")
            db.collection(u'CompletedMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId).set(doc.to_dict())

        # Get Notifications collection reference from match in ScheduledMatches
        not_data_ref = db.collection(u'LiveMatches').document(esportsMatchId)\
            .collection(u'Games').document(esportsGameId)\
            .collection(u'Notifications')
        notDocList = not_data_ref.get()

        for notDoc in notDocList:
            # Copy notification metadata to match in LiveMatches
            notificationId = notDoc.id
            data_ref = db.collection(u'LiveMatches').document(esportsMatchId)\
                .collection(u'Games').document(esportsGameId)\
                .collection(u'Notifications').document(notificationId)
            doc = data_ref.get()

            db.collection(u'CompletedMatches').document(esportsMatchId)\
                .collection(u'Games').document(esportsGameId)\
                .collection(u'Notifications').document(notificationId).set(doc.to_dict())

            while len(db.collection(u'CompletedMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId).collection(u'Notifications').document(notificationId).get().to_dict()) == 0:
                print("Match was empty in Firebase, retrying.")
                db.collection(u'CompletedMatches').document(esportsMatchId).collection(u'Games').document(esportsGameId).collection(u'Notifications').document(notificationId).set(doc.to_dict())

            # Delete notification metadata from ScheduledMatches
            db.collection(u'LiveMatches').document(esportsMatchId)\
                .collection(u'Games').document(esportsGameId)\
                .collection(u'Notifications').document(notificationId).delete()

        # Delete game metadata from LiveMatches
        db.collection(u'LiveMatches').document(esportsMatchId) \
            .collection(u'Games').document(esportsGameId).delete()

    # Delete match metadata from ScheduledMatches
    db.collection(u'LiveMatches').document(esportsMatchId).delete()

def matchIsScheduled(esportsMatchId):
    db = getFirestoreClient()

    doc_ref = db.collection(u'ScheduledMatches').document(esportsMatchId)
    doc = doc_ref.get()

    return doc.exists


def matchIsLive(esportsMatchId):
    db = getFirestoreClient()

    doc_ref = db.collection(u'LiveMatches').document(esportsMatchId)
    doc = doc_ref.get()

    return doc.exists


def matchIsCompleted(esportsMatchId):
    db = getFirestoreClient()

    doc_ref = db.collection(u'CompletedMatches').document(esportsMatchId)
    doc = doc_ref.get()

    return doc.exists

def addNotificationToGame(esportsMatchId, esportsGameId, text, playingTime, blueTeamCode, redTeamCode):
    db = getFirestoreClient()

    data = {
        u'text': text[:-100],
        u'playingTime': playingTime,
        u'blueTeamKills': text[-75:-73],
        u'redTeamKills': text[-66:-64],
        u'blueTeamGold': text[-56:-51],
        u'redTeamGold': text[-48:-43],
        u'blueTeamTowers': text[-35:-33],
        u'redTeamTowers': text[-26:-24],
        u'blueTeamDragons': text[-15:-13],
        u'redTeamDragons': text[-6:-4],
        u'blueSideTeam': blueTeamCode,
        u'redSideTeam': redTeamCode
    }

    db.collection(u'LiveMatches').document(esportsMatchId)\
        .collection(u'Games').document(esportsGameId)\
        .collection(u'Notifications').document(str(playingTime)).set(data)

def getCompletedMatchesIdList():
    db = getFirestoreClient()
    data_ref = db.collection(u'CompletedMatches')
    matchesDocList = data_ref.get()

    return [match.id for match in matchesDocList]

def changeNotifications():
    db = getFirestoreClient()

    data_ref = db.collection(u'CompletedMatches')
    matchesDocList = data_ref.get()

    for matchDoc in matchesDocList:
        data_ref = db.collection(u'CompletedMatches').document(matchDoc.id).collection(u'Games')
        gamesDocList = data_ref.get()

        print(matchDoc.to_dict()['teams'][0])

        blueSideCode = matchDoc.to_dict()['teams'][0]['code']
        redSideCode = matchDoc.to_dict()['teams'][1]['code']

        for gameDoc in gamesDocList:
            data_ref = db.collection(u'CompletedMatches').document(matchDoc.id).collection(u'Games').document(gameDoc.id).collection(u'Notifications')
            notificationsDocList = data_ref.get()

            for notificationDoc in notificationsDocList:
                if len(notificationDoc.to_dict().keys()) == 2:
                    db.collection(u'CompletedMatches').document(matchDoc.id).collection(u'Games').document(
                            gameDoc.id).collection(u'Notifications').document(notificationDoc.id).delete()

            # for notificationDoc in notificationsDocList:
            #     data = {
            #         u'blueSideTeam': blueSideCode,
            #         u'redSideTeam': redSideCode
            #     }
            #
            #     db.collection(u'CompletedMatches').document(matchDoc.id).collection(u'Games').document(
            #         gameDoc.id).collection(u'Notifications').document(notificationDoc.id).update(data)

def addBackupMatchMetadata(matchMetadata):
    db = getFirestoreClient()

    data = {
        u'metadata': matchMetadata
    }

    db.collection(u'BackupMatches').document(matchMetadata['id']).set(data)

def addBackupGameSnapshot(snapshotMetadata, snapshotNumber):
    db = getFirestoreClient()

    data = {
        u'metadata': snapshotMetadata
    }

    db.collection(u'BackupMatches').document(snapshotMetadata['esportsMatchId'])\
        .collection(u'Games').document(snapshotMetadata['esportsGameId'])\
        .collection(u'Snapshots').document(str(snapshotNumber)).set(data)

def sortCollections():
    db = getFirestoreClient()

    