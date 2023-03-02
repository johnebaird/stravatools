from django import forms

class UpdatableActivity(forms.Form):
    commute = forms.BooleanField(required=False)
    trainer = forms.BooleanField(required=False)
    hide_from_home = forms.BooleanField(required=False)
    description = forms.CharField(required=False, label='description', max_length=500)
    name = forms.CharField(label='name', max_length=100)
    gear_id = forms.ChoiceField()
       
    # def populate_bikes(self, choices):
    #     self.fields['gear_id'].choices = choices