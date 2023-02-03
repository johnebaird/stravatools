from django.db import models

# Create your models here.

class Bearer(models.Model):
    access_token = models.CharField(max_length=100)
    expires_at = models.DateTimeField()
    expires_in = models.DateTimeField()
    refresh_token = models.CharField(max_length=100)

class Bike(models.Model):
    id = models.CharField(max_length=20, primary_key=True)
    primary = models.BooleanField
    
    name = models.CharField(max_length=100)
    distance = models.IntegerField
    brand_name = models.CharField(max_length=100)
    model_name = models.CharField(max_length=100)
    description = models.CharField(max_length=200)

class User(models.Model):
    username = models.CharField(max_length=100)
    account_locked = models.BooleanField
    autochange_indoor_bike = models.BooleanField
    autochange_outdoor_bike = models.BooleanField
    bearer = models.ForeignKey(Bearer, on_delete=models.CASCADE)
    indoor_bike = models.ForeignKey(Bike, on_delete=models.CASCADE, related_name='indoor_bike')
    outdoor_bike = models.ForeignKey(Bike, on_delete=models.CASCADE, related_name='outdoor_bike')
    # password