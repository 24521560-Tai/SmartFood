using System.ComponentModel.DataAnnotations;

namespace MyAPI.Model
{
    public class Food
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(50)]
        public string Barcode { get; set; } = null!;

        [Required]
        [MaxLength(255)]
        public string Name { get; set; } = null!;

        [MaxLength(150)]
        public string? Brand { get; set; }

        public int CategoryId { get; set; }

        public string? ImageUrl { get; set; }

        [MaxLength(50)]
        public string? ImageSource { get; set; }

        public DateTime CreatedAt { get; set; }
            = DateTime.Now;

        public Category Category { get; set; } = null!;

        public ICollection<FoodItem> FoodItems { get; set; }
            = new List<FoodItem>();
    }
}