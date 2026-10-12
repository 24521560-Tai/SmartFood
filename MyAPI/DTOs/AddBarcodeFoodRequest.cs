using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace MyAPI.DTOs
{
    public class AddBarcodeFoodRequest
    {
        [Required]
        [RegularExpression(@"^\d{1,50}$")]
        public string Barcode { get; set; } = null!;

        [Required]
        [MaxLength(255)]
        public string Name { get; set; } = null!;

        [MaxLength(150)]
        public string? Brand { get; set; }

        [Required]
        public int CategoryId { get; set; }

        [Range(typeof(decimal), "0.01", "99999999")]
        public decimal Quantity { get; set; }

        [Required]
        [MaxLength(50)]
        public string Unit { get; set; } = null!;

        [Required]
        public DateTime ExpiryDate { get; set; }

        [MaxLength(500)]
        public string? Note { get; set; }

        public IFormFile? Image { get; set; }
    }
}