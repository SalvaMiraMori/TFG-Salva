import time

import firebase_admin
from colorama import Fore
from firebase_admin import credentials, messaging


def sendNotification(topic, gameAndTime, message):

    if not firebase_admin._apps:
        firebase_admin.initialize_app(credential=credentials.Certificate('secret_key.json'))

    message = messaging.Message(topic=topic, data={
        'gameAndTime': gameAndTime,
        'text': message
    })

    sentResponse = False

    while not sentResponse:
        try:
            response = messaging.send(message)
            sentResponse = True
        except Exception as e:
            print(Fore.RED + str(e) + Fore.RESET)
            time.sleep(1)

    print('Successfully sent message:', response)