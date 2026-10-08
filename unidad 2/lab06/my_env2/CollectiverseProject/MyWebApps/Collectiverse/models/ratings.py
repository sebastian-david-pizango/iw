from django.db import models
from django.contrib.auth.models import User
from django.core.validators import MinValueValidator, MaxValueValidator

from .publications import Publication


class Rating(models.Model):

    id_rating = models.BigAutoField(
        primary_key=True
    )

    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name="ratings"
    )

    publication = models.ForeignKey(
        Publication,
        on_delete=models.CASCADE,
        related_name="ratings"
    )

    user_rating = models.PositiveSmallIntegerField(
        validators=[
            MinValueValidator(1),
            MaxValueValidator(5)
        ]
    )

    publication_rating = models.PositiveSmallIntegerField(
        validators=[
            MinValueValidator(1),
            MaxValueValidator(5)
        ]
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
        db_table = "ratings"

    def __str__(self):
        return f"Rating {self.id_rating}"