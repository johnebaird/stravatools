import os
import requests
import logging
import json

from django.conf import settings

from django.core.mail.backends.base import BaseEmailBackend
from django.core.mail.message import sanitize_address

SENDGRID_API_KEY = os.environ['SENDGRID_API_KEY']
SENDGRID_FROM_ADDRESS = os.environ['SENDGRID_FROM_ADDRESS']

sendgridurl = "https://api.sendgrid.com/v3/mail/send"
headers = {"Authorization" : "Bearer " + SENDGRID_API_KEY,
           "Content-type": "application/json"}

logger = logging.getLogger(__name__)

class SendGrid(BaseEmailBackend):

    def send_messages(self, email_messages) -> int:
        
        if not email_messages:
            return 0
        
        count = 0

        for message in email_messages:
            encoding = message.encoding or settings.DEFAULT_CHARSET
            from_email = SENDGRID_FROM_ADDRESS
            recipients = [
                sanitize_address(addr, encoding) for addr in message.recipients()
            ]
            
            data = {"personalizations": [{"to": [{"email": recipients[0]}]}],
                    "from": {"email": SENDGRID_FROM_ADDRESS},
                    "subject": message.subject,
                    "content": [{"type": "text/plain", "value": message.body}]
                }
            
            data = json.dumps(data)
            
            response = requests.post(sendgridurl, headers=headers, data=data)

            if response.status_code == 202: 
                count += 1
            else:
                logger.error("Sendgrid POST returned %i %s", response.status_code, response.text)
            
        return count

        
                     

    
