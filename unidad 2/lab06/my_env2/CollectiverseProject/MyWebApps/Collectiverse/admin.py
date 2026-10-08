from django.contrib import admin

from .models.products import Product
from .models.publications import Publication
from .models.ratings import Rating


admin.site.register(Product)
admin.site.register(Publication)
admin.site.register(Rating)