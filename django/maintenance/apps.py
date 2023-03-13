import sys
import os
import logging

from django.apps import AppConfig

logger = logging.getLogger(__name__)

class MaintenanceConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'maintenance'

    def ready(self):

        # don't need to check for this if running makemigrations, migrate, etc..
        if 'runserver' in sys.argv:
            if 'SENDGRID_API_KEY' not in os.environ:
                logger.error("SENDGRID_API_KEY environment variable needs to be set")
                exit()
            
            if 'SENDGRID_FROM_ADDRESS' not in os.environ:
                logger.error("SENDGRID_FROM_ADDRESS environment variable needs to be set")
                exit()
