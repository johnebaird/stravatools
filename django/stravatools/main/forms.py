from django import forms

# class RegisterForm(forms.Form):
#     username = forms.CharField(label='Username/Email', max_length=150)
#     password = forms.PasswordInput()

#from https://developers.strava.com/docs/reference/#api-models-SportType
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

# SportType = [('Ride', 'Ride'), ('Yoga', 'Yoga'), ('Run', 'Run')]

class UpdatableActivity(forms.Form):
    commute = forms.BooleanField(required=False)
    trainer = forms.BooleanField(required=False)
    hide_from_home = forms.BooleanField(required=False)
    description = forms.CharField(required=False, label='description', max_length=500)
    name = forms.CharField(label='name', max_length=100)
    sport_type = forms.ChoiceField(choices=SportType)
    gear_id = forms.ChoiceField()
       
    def populate_bikes(self, choices):
        self.fields['gear_id'].choices = choices