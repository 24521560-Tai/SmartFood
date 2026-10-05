using Microsoft.AspNetCore.Http;
using System.ComponentModel.DataAnnotations;

namespace MyAPI.DTOs
{
    public class AddManualFoodRequest
    {
        [Required]
        [MaxLength(255)]
        public string Name { get; set; } = null!;

        [Required]
        public int CategoryId { get; set; }

        [Required]
        public decimal Quantity { get; set; }

        [Required]
        [MaxLength(50)]
        public string Unit { get; set; } = null!;

        [Required]
        public DateTime ExpiryDate { get; set; }

        [MaxLength(500)]
        public string? Note { get; set; }

        // Ảnh không bắt buộc
        public IFormFile? Image { get; set; }
    }
}