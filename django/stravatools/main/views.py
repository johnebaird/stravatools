from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm

# Create your views here.

def index(request):
    return render(request, 'main/index.html')
    
def register(request):
    # redirect to index page if they haven't authed to strava yet
    if 'code' not in request.session: 
        return redirect(index)

    if request.method == 'POST':

        form = UserCreationForm(request.POST)
    
        if form.is_valid():
            form.save()
    
    else:
        form = UserCreationForm()
    
    return render(request, 'registration/register.html', {'form': form, 'code':request.session['code']})
    

def exchange_token(request):
    request.session['code'] = request.GET['code']
    return redirect(register)

