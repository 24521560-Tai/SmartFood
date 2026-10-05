using System.ComponentModel.DataAnnotations;

namespace MyAPI.Model
{
    public class Category
    {
        public int Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string Name { get; set; } = null!;

        public ICollection<Food> Foods { get; set; }
            = new List<Food>();

        public ICollection<FoodItem> CustomFoodItems { get; set; }
            = new List<FoodItem>();
    }
}