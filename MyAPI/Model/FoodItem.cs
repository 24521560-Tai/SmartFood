using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace MyAPI.Model
{
    public class FoodItem
    {
        public int Id { get; set; }

        // Chủ sở hữu
        public int UserId { get; set; }

        // Có giá trị nếu thêm bằng barcode
        public int? FoodId { get; set; }

        // Có giá trị nếu thêm thủ công
        [MaxLength(255)]
        public string? CustomName { get; set; }

        public int? CustomCategoryId { get; set; }

        public string? CustomImageUrl { get; set; }

        [Column(TypeName = "decimal(10,2)")]
        public decimal Quantity { get; set; }

        [Required]
        [MaxLength(50)]
        public string Unit { get; set; } = null!;

        public DateTime ExpiryDate { get; set; }

        [MaxLength(500)]
        public string? Note { get; set; }

        public DateTime AddedAt { get; set; }
            = DateTime.Now;


        // Navigation Properties

        public User User { get; set; } = null!;

        public Food? Food { get; set; }

        public Category? CustomCategory { get; set; }
    }
}