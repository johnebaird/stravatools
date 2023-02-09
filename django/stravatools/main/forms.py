from django import forms

class RegisterForm(forms.Form):
    username = forms.CharField(label='Username/Email', max_length=150)
    password = forms.PasswordInput()
    