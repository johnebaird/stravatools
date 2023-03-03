import logging
import json

from django.core.management.base import BaseCommand
from django.utils import timezone

from defaultbikes.models import DefaultBike
from main.models import Logging
from main import stravaapi

logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = "background task to update user's indoor/outdoor bikes automatically"
    
    def handle(self, *args, **kwargs):
        logger.debug("Running auto update bike job")

        bikechanges = DefaultBike.objects.filter(autochange_indoor_bike=True) | DefaultBike.objects.filter(autochange_outdoor_bike=True)

        for changes in bikechanges:
            stravaapi.refreshBearer(changes.profile.bearer)
            access_token = changes.profile.bearer.access_token
            activities = stravaapi.getActivities(access_token, 1)
            logging = []

            if changes.autochange_indoor_bike:
                bike = {'id': changes.indoor_bike.id, 'name': changes.indoor_bike.name}
                logging += stravaapi.changeActivitiesPage(access_token, activities, bike, "indoor")
                
            if changes.autochange_outdoor_bike:
                bike = {'id': changes.outdoor_bike.id, 'name': changes.outdoor_bike.name}
                logging += stravaapi.changeActivitiesPage(access_token, activities, bike, "outdoor")

            for logentry in logging:
                    Logging.objects.create(datetime=timezone.now(), application='autobikechange', profile=changes.profile, message=logentry)
            
            Logging.objects.create(datetime=timezone.now(), profile=changes.profile, 
                                   message="Bike Autochange task ran and changed " + str(len(logging)) + "bikes",
                                   application='autobikechange')

