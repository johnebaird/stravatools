from datetime import datetime

from django import forms
from .models import DefaultBike, Bike

class DefaultBikesForm(forms.ModelForm):

    def __init__(self, profile, *args, **kwargs):
        super(DefaultBikesForm, self).__init__(*args, **kwargs)
        if profile is None:
            for field in self.fields.values():
                field.disabled = True
        else:        
            self.fields['indoor_bike'].queryset = Bike.objects.filter(profile=profile)
            self.fields['outdoor_bike'].queryset = Bike.objects.filter(profile=profile)

    class Meta:
        model = DefaultBike
        fields = ['indoor_bike', 'outdoor_bike', 'autochange_indoor_bike', 'autochange_outdoor_bike']
        labels = {
            'autochange_indoor_bike': 'Change all new indoor activities to indoor bike',
            'autochange_outdoor_bike': 'Change all new outdoor activities to outdoor bike',
        }

class DateInput(forms.DateInput):
    input_type = 'date'

def date_not_in_future(value):
    if value > datetime.now().date():
        raise forms.ValidationError('Value cannot be in future', code='invalid')
    return value

class ManualBikeCorrection(forms.Form):
    after = forms.DateField(widget=DateInput(), validators=[date_not_in_future], label="from:")
    before = forms.DateField(widget=DateInput(), validators=[date_not_in_future], label='to:')
    activitytype = forms.ChoiceField(choices=(("indoor", "Indoor"), ('outdoor',"Outdoor")), label='for ride type')
    bike = forms.ChoiceField(label='to bike')

    def clean(self):
        cleaned_data = super().clean()

        after = cleaned_data.get("after")
        before = cleaned_data.get("before")

        # want > not >= since before=after works to target a single day        
        if after and before and after > before:
            msg = 'From date must be before to date'
            self.add_error('after', msg)
            self.add_error('before', msg)

