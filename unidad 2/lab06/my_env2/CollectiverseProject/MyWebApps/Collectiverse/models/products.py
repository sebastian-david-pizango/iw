from django.db import models


class Product(models.Model):

    id_product = models.BigAutoField(primary_key=True)

    name = models.CharField(
        max_length=200
    )

    product_type = models.CharField(
        max_length=100
    )

    image = models.ImageField(
        upload_to="products/",
        null=True,
        blank=True
    )

    quantity = models.PositiveIntegerField(
        default=1
    )

    price = models.DecimalField(
        max_digits=10,
        decimal_places=2
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
        db_table = "products"

    def __str__(self):
        return self.name