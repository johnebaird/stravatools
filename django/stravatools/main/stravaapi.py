import requests
import os
import logging
import json

from .models import Bearer
from datetime import datetime

logger = logging.getLogger(__name__)

STRAVA_CLIENT_ID = os.getenv("STRAVA_CLIENT_ID")
STRAVA_CLIENT_SECRET = os.getenv("STRAVA_CLIENT_SECRET")

stravaurl = "https://www.strava.com/api/v3"

def refreshBearer(bearer: Bearer) -> None:
    
    if (round(datetime.utcnow().timestamp()) + 3500) < int(bearer.expires_at):
        return 
    
    payload = {'client_id': STRAVA_CLIENT_ID, 
                'client_secret': STRAVA_CLIENT_SECRET, 
                'grant_type': 'refresh_token',
                'refresh_token': bearer.refresh_token}

    r = requests.post(stravaurl + "/oauth/token", data=payload)

    logger.debug("Post to refresh Bearer token returned: " + r.text)

    returned_data = json.loads(r.text)

    if 'errors' in returned_data:
        logger.error("Error refreshing Bearer token")
        return 
    
    bearer.load_json(returned_data)
    bearer.save()


def getBearerFromCode(code: str) -> Bearer:
    
    logger.info("Querying Strava API for bearer code..")

    logger.debug("Using Strava Client: " + STRAVA_CLIENT_ID)
    logger.debug("Using Strava Client Secret: " + STRAVA_CLIENT_SECRET)
    
    payload = {'client_id': STRAVA_CLIENT_ID, 
                'client_secret': STRAVA_CLIENT_SECRET, 
                'code': code, 
                'grant_type': 'authorization_code'}

    r = requests.post(stravaurl + "/oauth/token", data=payload)

    logger.debug("Post for Bearer Code returned: " + r.text)

    returned_data = json.loads(r.text)

    if 'errors' in returned_data:
        logger.error("Error querying for Bearer Token")
        return None

    return returned_data

def getAthlete(access_token: str) -> str:
    logger.info("Querying for Athelete..")

    auth = {"Authorization" : "Bearer " + access_token}

    logger.debug("using access_token: " + str(auth))

    r = requests.get(stravaurl + "/athlete", headers=auth)

    logger.debug("GET for Athelete info returned: " + r.text)

    return json.loads(r.text)

def getActivities(access_token: str, page: int) -> dict:
    logger.info("Querying for athlete activities")

    auth = {"Authorization" : "Bearer " + access_token}
    params = {"page": page}

    logger.debug("using access_token: " + str(auth))

    r = requests.get(stravaurl + "/activities", headers=auth, params=params)

    return json.loads(r.text)

def getActivityFromId(access_token: str, id: int) -> dict:
    logger.info("Querying for activity" + str(id))

    auth = {"Authorization": "Bearer " + access_token}
    
    logger.debug("using access_token: " + str(auth))
    r = requests.get(stravaurl + "/activities/" + str(id), headers=auth)

    data = json.loads(r.text)
    del data['map']
    logger.debug("GET for specific activity returned" + str(data))

    return data

def getGear(access_token:str) -> dict:
    logger.info("Querying for gear..")

    auth = {"Authorization": "Bearer " + access_token}

    r = requests.get(stravaurl + "/gear", headers=auth)

    logger.debug("Gear query returns: " + r.text)

    return json.loads(r.text)


def updateActivity(access_token: str, id:int, data:dict) -> None:
    logger.info("Updating activity " + str(id))
    logger.debug(" with: " + str(data))

    auth = {"Authorization": "Bearer " + access_token}

    r = requests.put(stravaurl + "/activities/" + str(id), data=data, headers=auth)

    logger.debug("Activity update PUT returns: " + r.text)

    return