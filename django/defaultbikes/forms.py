from django import forms
from .models import DefaultBike, Bike

class DefaultBikesForm(forms.ModelForm):

    def __init__(self, profile, *args, **kwargs):
        super(DefaultBikesForm, self).__init__(*args, **kwargs)
        if profile is not None:
            self.fields['indoor_bike'].queryset = Bike.objects.filter(profile=profile)
            self.fields['outdoor_bike'].queryset = Bike.objects.filter(profile=profile)


    class Meta:
        model = DefaultBike
        fields = ['indoor_bike', 'outdoor_bike', 'autochange_indoor_bike', 'autochange_outdoor_bike']