from datetime import datetime, timezone

from django.test import TestCase
from django.test import Client
from django.urls import reverse
from django.contrib.auth import authenticate

from django.contrib.auth.models import User
from main.models import Bearer, Logging

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

        






        





        


