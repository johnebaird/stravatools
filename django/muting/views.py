import logging

from django.shortcuts import render, redirect
from django.contrib.auth.decorators import login_required
from django.forms import modelformset_factory

from main.utils import checkbearer
from main.models import Logging

from .models import Muting
from .forms import MutingForm, MutingFormSet

# Create your views here.

logger = logging.getLogger(__name__)

@login_required
def muting(request):

    if not checkbearer(request): redirect("main:index")
    if not request.session['stravawrite']: return redirect('activities:activities')

    changelog = Logging.objects.filter(profile=request.user.profile, application='muting').order_by('datetime')[:10]

    ActivityMutingFormSet = modelformset_factory(Muting, formset=MutingFormSet, form=MutingForm, extra=1, can_delete=True)

    if request.method == 'POST':

        formset = ActivityMutingFormSet(request.user.profile, request.POST, request.FILES)
        if formset.is_valid():
            logger.debug("muting form is valid")
            formset.save()
            formset = ActivityMutingFormSet(request.user.profile)
            
            
    else:
        formset = ActivityMutingFormSet(request.user.profile)

    return render(request, 'muting/muting.html', {'formset': formset, 'changelog': changelog})

