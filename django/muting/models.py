from django.db import models
from main.models import Profile

# Create your models here.

# from https://developers.strava.com/docs/reference/#api-models-SportType
SportType = [("AlpineSki", "AlpineSki"),  ("BackcountrySki", "BackcountrySki"),  ("Badminton", "Badminton"),  
             ("Canoeing", "Canoeing"),  ("Crossfit", "Crossfit"),  ("EBikeRide", "EBikeRide"),  ("Elliptical", "Elliptical"),  
             ("EMountainBikeRide", "EMountainBikeRide"),  ("Golf", "Golf"),  ("GravelRide", "GravelRide"),  
             ("Handcycle", "Handcycle"),  ("HighIntensityIntervalTraining", "HighIntensityIntervalTraining"),  
             ("Hike", "Hike"),  ("IceSkate", "IceSkate"),  ("InlineSkate", "InlineSkate"),  ("Kayaking", "Kayaking"),  
             ("Kitesurf", "Kitesurf"),  ("MountainBikeRide", "MountainBikeRide"),  ("NordicSki", "NordicSki"),  
             ("Pickleball", "Pickleball"),  ("Pilates", "Pilates"),  ("Racquetball", "Racquetball"),  ("Ride", "Ride"),  
             ("RockClimbing", "RockClimbing"),  ("RollerSki", "RollerSki"),  ("Rowing", "Rowing"),  ("Run", "Run"),  
             ("Sail", "Sail"),  ("Skateboard", "Skateboard"), ("Snowboard", "Snowboard"),  ("Snowshoe", "Snowshoe"),  
             ("Soccer", "Soccer"),  ("Squash", "Squash"),  ("StairStepper", "StairStepper"),  ("StandUpPaddling", "StandUpPaddling"),  
             ("Surfing", "Surfing"),  ("Swim", "Swim"), ("TableTennis", "TableTennis"),  ("Tennis", "Tennis"),  
             ("TrailRun", "TrailRun"),  ("Velomobile", "Velomobile"),  ("VirtualRide", "VirtualRide"),  
             ("VirtualRow", "VirtualRow"),  ("VirtualRun", "VirtualRun"),  ("Walk", "Walk"),  ("WeightTraining", "WeightTraining"),  
             ("Wheelchair", "Wheelchair"),  ("Windsurf", "Windsurf"),  ("Workout", "Workout"),  ("Yoga", "Yoga")]


class Muting(models.Model):
    profile = models.ForeignKey(Profile, on_delete=models.CASCADE)
    activitytype = models.CharField(max_length=40, choices=SportType)
    duration = models.IntegerField(blank=True, null=True)
    muteall = models.BooleanField()
    lastupdatedactivity = models.IntegerField(blank=True, null=True)

    def __str__(self):
        if self.muteall:
            return f"{self.profile} all {self.activitytype}"
        else:
            return f"{self.profile} <{self.duration} {self.activitytype}"

