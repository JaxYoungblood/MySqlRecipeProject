package recipes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import jdk.jfr.Category;
import recipes.dao.DbConnection;
import recipes.entity.Ingredient;
import recipes.entity.Recipe;
import recipes.entity.Step;
import recipes.entity.Unit;
import recipes.exception.DbException;
import recipes.service.RecipeService;

public class Recipes {
	private Scanner scanner = new Scanner(System.in);
	private RecipeService recipeService = new RecipeService();
	private Recipe curRecipe;

	// @formatter:off
	private List<String> operations = List.of(
			"1) Create and Populate all tables",
			"2) Add a Recipe",
			"3) List Recipes",
			"4) Select Working Recipe",
			"5) Add Ingredient to Current Recipe",
			"6) Add Step to Current Recipe",
			"7) Add Category to Current Recipe"
			);
	// @formatter:on


	public static void main(String[] args) {
		new Recipes().displayMenu();
	}

	private void displayMenu() {
		boolean done = false;

		while (!done) {
			try {
				int operation = getOperation();

				switch (operation) {
				case -1:
					done = exitMenu();
					break;
				case 1:
					createTables();
					break;
					
				case 2:
					addRecipe();
					break;
					
				case 3:
					listRecipes();
					break;
					
				case 4:
					setCurrentRecipe();
					break;
					
				case 5:
					addIngredientToCurrentRecipe();
					break;
					
				case 6:
					addStepToCurrentRecipe();
					break;
					
				case 7:
					addCategoryToCurrentRecipe();
					break;
					
				default:
					System.out.println("\n" + operation + " is not valid. Try again!");
					break;
						
				}// end TRY
			} catch (Exception e) {
				System.out.println("\nError: " + e.toString() + " Try again!");
			} // end CATCH
		} // end WHILE
	}// end displayMenu

	private void addCategoryToCurrentRecipe() {
	    if (Objects.isNull(curRecipe)) {
	      System.out.println("\nPlease select a recipe first.");
	      return;
	    }//end IF isNull

	    List<Category> categories = recipeService.fetchCategories();

	    categories.forEach(
	        category -> System.out.println("   " + category.getCategoryName()));

	    String category = getStringInput("Enter the category to add");

	    if (Objects.nonNull(category)) {
	      recipeService.addCategoryToRecipe(curRecipe.getRecipeId(), category);
	      curRecipe = recipeService.fetchRecipeById(curRecipe.getRecipeId());
	    }//end IF nonNull
	  }//end METHOD addCategoryToCurrentRecipe

	private void addStepToCurrentRecipe() {
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nPlease select a recipe first!");
			return;
		}//end IF
		
		String stepText = getStringInput("Enter the step text");
		
		if(Objects.nonNull(stepText)) {
			Step step = new Step();
			
			step.setRecipeId(curRecipe.getRecipeId());
			step.setStepText(stepText);
			
			recipeService.addStep(step);
			curRecipe = recipeService.fetchRecipeById(step.getRecipeId());
		}//end METHOD addStepToCurrentRecipe
		
		
	}//end METHOD addStepToCurrentRecipe

	private void addIngredientToCurrentRecipe() {
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nPlease select a recipe first");
			return;
		}//end IF
		
		String name = getStringInput("Enter the Ingredient Name");
		String instruction = getStringInput("Enter an Instruction, If Any (finely chopped, etc)");
		Double inputAmount = getDoubleInput("Enter the Ingredient Amount (Enter the Ingredient Amount(numbers only)");
		List<Unit> units = recipeService.fetchUnits();
		
		BigDecimal amount = Objects.isNull(inputAmount) ? null : new BigDecimal(inputAmount).setScale(2);
		
		System.out.println("Units:");
		units.forEach(unit -> System.out.println("    " + unit.getUnitId() + ": " + unit.getUnitNameSingular() + "(" + unit.getUnitNamePlural() + ")"));

		Integer unitId = getIntInput("Enter a unitID (press ENTER for none)");
		
		Unit unit = new Unit();
		unit.setUnitId(unitId);
		
		Ingredient ingredient = new Ingredient();
		
		ingredient.setRecipeId(curRecipe.getRecipeId());
		ingredient.setUnit(unit);
		ingredient.setIngredientName(name);
		ingredient.setInstruction(instruction);
		ingredient.setAmount(amount);
		
		recipeService.addIngredient(ingredient);
		curRecipe = recipeService.fetchRecipeById(ingredient.getRecipeId());
		
	}//end METHOD addIngredientToCurrentRecipe

	private void setCurrentRecipe() {
		List<Recipe> recipes = listRecipes();
		
		Integer recipeId = getIntInput("Select a recipe ID");
		
		curRecipe = null;
		
		for(Recipe recipe : recipes) {
			if(recipe.getRecipeId().equals(recipeId)) {
				curRecipe = recipeService.fetchRecipeById(recipeId);
				break;
			}//end IF
		}//end FOR
		
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nInvalid recipe selected");
		}//end IF
	}//end METHOD setCurrentRecipe

	private List<Recipe> listRecipes() {
		List<Recipe> recipes = recipeService.fetchRecipes();
		
		System.out.println("\nRecipes:");
		
		recipes.forEach(recipe -> System.out.println("    " + recipe.getRecipeId() + ": " + recipe.getRecipeName()));
		
		return recipes;
	}//end METHOD listRecipes
	
	private void addRecipe() {
		String name = getStringInput("Enter the recipe name");
		String notes = getStringInput("Enter the recipe notes");
		Integer numServings = getIntInput("Enter the number of servings");
		Integer prepMinutes = getIntInput("Enter prep time in minutes");
		Integer cookMinutes = getIntInput("Enter cook time in minutes");
		
		LocalTime prepTime = minutesToLocalTime(prepMinutes);
		LocalTime cookTime = minutesToLocalTime(cookMinutes);
		
		Recipe recipe = new Recipe();
		
		recipe.setRecipeName(name);
		recipe.setNotes(notes);
		recipe.setNumServings(numServings);
		recipe.setPrepTime(prepTime);
		recipe.setCookTime(cookTime);
		
		Recipe dbRecipe = recipeService.addRecipe(recipe);
		System.out.println("You added this recipe:\n" + dbRecipe);
		
		curRecipe = recipeService.fetchRecipeById(dbRecipe.getRecipeId());
	}//end METHOD addRecipe

	private LocalTime minutesToLocalTime(Integer numMinutes) {
		int min = Objects.isNull(numMinutes) ? 0 : numMinutes;
		int hours = min / 60;
		int minutes = min % 60;
		
		return LocalTime.of(hours, minutes);
	}//end METHOD minutesToLocalTime

	private void createTables() {
		recipeService.createAndPopulateTables();
		System.out.println("\nTables created and populated");
	}//end METHOD createTables

	private boolean exitMenu() {
		System.out.println("\nExiting the menu");
		return true;
	}//end METHOD exitMenu

	private int getOperation() {
		printOperations();
		Integer op = getIntInput("\nEnter an operation number(Press Enter to QUIT)");

		return Objects.isNull(op) ? -1 : op;
	}//end METHOD getOperation

	private void printOperations() {
		System.out.println();
		System.out.println("Here's what you can do:");

		operations.forEach(op -> System.out.println("   " + op));
		
		if(Objects.isNull(curRecipe)) {
			System.out.println("\n**You are not working with a recipe**");
		}//end IF
		else {
			System.out.println("\n**You are working with the recipe " + curRecipe + "**");
		}//end ELSE
	}//end METHOD printOperation

	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}//end IF

		try {
			return Integer.parseInt(input);
		}//end TRY
		catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}//end CATCH
	}//end METHOD getIntInput
	
	private Double getDoubleInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}//end IF

		try {
			return Double.parseDouble(input);
		}//end TRY
		catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}//end CATCH
	}//end METHOD getDoubleInput

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String line = scanner.nextLine();

		return line.isBlank() ? null : line.trim();

	}//end METHOD getStringInput

}//end CLASS
