import requests
import os
import logging
import json

from .models import Bearer
from django.core import serializers

logger = logging.getLogger(__name__)

STRAVA_CLIENT_ID = os.getenv("STRAVA_CLIENT_ID")
STRAVA_CLIENT_SECRET = os.getenv("STRAVA_CLIENT_SECRET")

stravaurl = "https://www.strava.com/api/v3"

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

def getAthlete(bearer: Bearer) -> str:
    logger.info("Querying for Athelete..")

    auth = {"Authorization" : "Bearer " + bearer.access_token}

    r = requests.get(stravaurl + "/athlete/", headers=auth)

    logger.debug("Post for Athelete info returned: " + r.text)

    return r.text



