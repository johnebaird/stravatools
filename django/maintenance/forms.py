from django.forms import ModelForm, BaseModelFormSet
from .models import Reminder
from main.models import Bike

class ReminderFormSet(BaseModelFormSet):
    def __init__(self, profile, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.queryset = Reminder.objects.filter(profile=profile)
        self.form_kwargs = {'profile': profile}

class ReminderForm(ModelForm):

    def __init__(self, profile, *args, **kwargs):
        super(ReminderForm, self).__init__(*args, **kwargs)
        self.fields['bike'].queryset = Bike.objects.filter(profile=profile)
        self.instance.profile = profile
    
    class Meta:   
        model = Reminder 
        fields = ("bike", "email", "message", "every",)
