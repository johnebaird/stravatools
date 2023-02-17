from django import forms

# class RegisterForm(forms.Form):
#     username = forms.CharField(label='Username/Email', max_length=150)
#     password = forms.PasswordInput()
    
class UpdatableActivity(forms.Form):
    commute = forms.BooleanField()
    trainer = forms.BooleanField()
    hide_from_homn = forms.BooleanField()
    description = forms.TextField(label='description', max_length=500)
    name = forms.CharField(label='name', max_length=100)
    sport_type = forms.TextChoices 
    gear_id = ? 
