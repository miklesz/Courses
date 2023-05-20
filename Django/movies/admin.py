from django.contrib import admin

# Register your models here.
from movies.models import Movie  # NOWE

admin.site.register(Movie)  # NOWE
