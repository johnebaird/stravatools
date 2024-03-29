from django.apps import AppConfig
import os
import logging
import sys

logger = logging.getLogger(__name__)

class MainConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'main'

    def ready(self):

        # don't need to check for this if running makemigrations, migrate, etc..
        if 'runserver' in sys.argv:
            if 'STRAVA_CLIENT_ID' not in os.environ:
                logger.error("STRAVA_CLIENT_ID environment variable needs to be set")
                exit()
            
            if 'STRAVA_CLIENT_SECRET' not in os.environ:
                logger.error("STRAVA_CLIENT_SECRET environment variable needs to be set")
                exit()

            
