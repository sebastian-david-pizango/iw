from django.db import models
from django.contrib.auth.models import User

from .products import Product


class Publication(models.Model):

    PUBLICATION_TYPE_CHOICES = [
        ("sale", "Sale"),
        ("trade", "Trade"),
        ("both", "Sale and Trade"),
    ]

    id_publication = models.BigAutoField(
        primary_key=True
    )

    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name="publications"
    )

    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="publications"
    )

    publication_type = models.CharField(
        max_length=20,
        choices=PUBLICATION_TYPE_CHOICES,
        default="sale"
    )

    status = models.BooleanField(
        default=True
    )

    created = models.DateTimeField(
        auto_now_add=True
    )

    modified = models.DateTimeField(
        auto_now=True
    )

    created_id = models.IntegerField(
        null=False
    )

    modified_id = models.IntegerField(
        null=True,
        blank=True
    )

    class Meta:
        db_table = "publications"

    def __str__(self):
        return f"{self.product.name} - {self.user.username}"
