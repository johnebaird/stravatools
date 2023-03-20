from datetime import datetime, timezone
from unittest.mock import patch

from django.test import TestCase
from django.test import Client
from django.urls import reverse
from django.contrib.auth import authenticate

from django.contrib.auth.models import User
from main.models import Bearer, Logging

from main import stravaapi

# Create your tests here.


class IndexPageTests(TestCase):
    def setUp(self):
        self.client = Client()
        
    def test_index_anonymous(self):
        response = self.client.get(reverse('main:index'))

        self.assertEqual(response.status_code, 200)
        self.assertContains(response, "Welcome to Stravatools")

    def test_index_usernobearer(self):
        user = User.objects.get_or_create(username='testuser')[0]
        self.client.force_login(user)
                
        response = self.client.get(reverse('main:index'))

        self.assertEqual(response.status_code, 200)
        self.assertContains(response, "Welcome to Stravatools")

    def test_index_userwithbearer(self):
        user = User.objects.get_or_create(username='testuser')[0]
        user.profile.bearer = Bearer(expires_at=0, expires_in=0, access_token="", refresh_token="")
        user.profile.bearer.save()

        self.client.force_login(user)

        response = self.client.get(reverse('main:index'))

        self.assertRedirects(response, reverse('activities:activities'), fetch_redirect_response=False)
        
class TestLoggedOutPage(TestCase):
        
    def setUp(self):
        self.client = Client()

    def test_index_usernobearer(self):
        user = User.objects.get_or_create(username='testuser')[0]
        self.client.force_login(user)

        response = self.client.get(reverse('logout'))

        self.assertEqual(response.status_code, 200)
        self.assertContains(response, "Logged out")

        # we're logged out so check that we can't get activities
        response = self.client.get(reverse('activities:activities'))
        self.assertRedirects(response, reverse('main:index'), fetch_redirect_response=False)


class TestProfilePage(TestCase):
    def setUp(self):
        self.client = Client()

    def test_require_login(self):

        response = self.client.get(reverse('main:profile'))
        self.assertRedirects(response, reverse('login')+"?next="+reverse('main:profile'), status_code=302, target_status_code=200)

    def test_profile_shows_data(self):

        user = User.objects.get_or_create(username='testuser')[0]
        Logging.objects.create(datetime=datetime.now(tz=timezone.utc), profile=user.profile, message='test log message')

        self.client.force_login(user)

        response = self.client.get(reverse('main:profile'))
        
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'testuser')
        self.assertContains(response, 'test log message')

@patch("main.views.stravaapi", autospec=True)
class TestExchangeToken(TestCase):
    
    def setUp(self):
        self.client = Client()

    def test_exhange_token_no_data(self, mock_stravaapi):
        # called with no data which is invalid so we should redirect to index

        response = self.client.get(reverse('main:exchange_token'))

        self.assertRedirects(response, reverse('main:index'), fetch_redirect_response=True, target_status_code=200)


    def test_exchange_token_no_profile_read(self, mock_stravaapi):
        # strava was authed and got data but read was unchecked, need read so we redirect to index
        getdata = {'state': [''], 
                   'code': ['aaaaabbbbbcccccdddddeeeeed40d9512b7d41b1'], 
                   'scope': ['read,activity:write,activity:read_all'],
                   }

        response = self.client.get(reverse('main:exchange_token'), data=getdata)
        
        self.assertRedirects(response, reverse('main:index'), fetch_redirect_response=True, target_status_code=200)

    def test_exchange_token_no_activity_read(self, mock_stravaapi):
        # strava was authed and got data but read was unchecked, need read so we redirect to index
        getdata = {'state': [''], 
                   'code': ['aaaaabbbbbcccccdddddeeeeed40d9512b7d41b1'], 
                   'scope': ['read,activity:write,profile:read_all'],
                   }
        
        response = self.client.get(reverse('main:exchange_token'), data=getdata)
        
        self.assertRedirects(response, reverse('main:index'), fetch_redirect_response=True, target_status_code=200)


    def test_exchange_token_read_only(self, mock_stravaapi):
        # strava was authed for read only, 'stravawrite' should be false and redirect to register page
        
        getdata = {'state': [''], 
                   'code': ['aaaaabbbbbcccccdddddeeeeed40d9512b7d41b1'], 
                   'scope': ['read,activity:read_all,profile:read_all'],
                   }
        
        mock_stravaapi.getBearerFromCode.return_value = {'token_type': 'Bearer', 'expires_at': 1679343993, 'expires_in': 12399, 'refresh_token': '1401a4493980ce24c2ac755f23a50fbbbbbaaaaa', 'access_token': '652e222934d95f7cdadbc89b7e5bde9b27dff8a6', 'athlete': {'id': 12345, 'username': None, 'resource_state': 2, 'firstname': 'Test', 'lastname': 'User', 'bio': '', 'city': 'Saint John', 'state': 'New Brunswick', 'country': 'Canada'}}

        response = self.client.get(reverse('main:exchange_token'), data=getdata)
        
        self.assertTrue(mock_stravaapi.getBearerFromCode.called)
        self.assertFalse(self.client.session["stravawrite"])
        self.assertTrue('bearer' in self.client.session)
        self.assertRedirects(response, reverse('main:register'), fetch_redirect_response=True, target_status_code=200)
        

    def test_exchange_token_write(self, mock_stravaapi):
        # strava was authed for write, 'stravawrite' should be true and redirect to register page
        # 'bearer' should be saved to session
        
        getdata = {'state': [''], 
                   'code': ['aaaaabbbbbcccccdddddeeeeed40d9512b7d41b1'], 
                   'scope': ['read,activity:write,activity:read_all,profile:read_all'],
                   }
      
        mock_stravaapi.getBearerFromCode.return_value = {'token_type': 'Bearer', 'expires_at': 1679343993, 'expires_in': 12399, 'refresh_token': '1401a4493980ce24c2ac755f23a50fbbbbbaaaaa', 'access_token': '652e222934d95f7cdadbc89b7e5bde9b27dff8a6', 'athlete': {'id': 12345, 'username': None, 'resource_state': 2, 'firstname': 'Test', 'lastname': 'User', 'bio': '', 'city': 'Saint John', 'state': 'New Brunswick', 'country': 'Canada'}}

        response = self.client.get(reverse('main:exchange_token'), data=getdata)
        
        self.assertTrue(mock_stravaapi.getBearerFromCode.called)
        self.assertTrue(self.client.session["stravawrite"])
        self.assertTrue('bearer' in self.client.session)
        self.assertRedirects(response, reverse('main:register'), fetch_redirect_response=True, target_status_code=200)

    def test_overwrite_bearer_token(self, mock_stravaapi):
        # if we're logged in and have a existing bearer token the new bearer should overwrite it

        getdata = {'state': [''], 
                   'code': ['aaaaabbbbbcccccdddddeeeeed40d9512b7d41b1'], 
                   'scope': ['read,activity:write,activity:read_all,profile:read_all'],
                   }
      
        mock_stravaapi.getBearerFromCode.return_value = {'token_type': 'Bearer', 'expires_at': 1679343993, 'expires_in': 12399, 'refresh_token': '1401a4493980ce24c2ac755f23a50fbbbbbaaaaa', 'access_token': '652e222934d95f7cdadbc89b7cccccbbbbbaaaaa', 'athlete': {'id': 12345, 'username': None, 'resource_state': 2, 'firstname': 'Test', 'lastname': 'User', 'bio': '', 'city': 'Saint John', 'state': 'New Brunswick', 'country': 'Canada'}}

        user = User.objects.get_or_create(username='testuser')[0]
        user.profile.bearer = Bearer(expires_at=0, expires_in=0, access_token="badtoken", refresh_token="")
        user.profile.bearer.save()

        self.client.force_login(user)

        response = self.client.get(reverse('main:exchange_token'), data=getdata)

        self.assertTrue(mock_stravaapi.getBearerFromCode.called)
        self.assertEquals(user.profile.bearer.access_token, "652e222934d95f7cdadbc89b7cccccbbbbbaaaaa")






        






        





        


