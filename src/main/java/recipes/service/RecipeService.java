package recipes.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import jdk.jfr.Category;
import recipes.dao.RecipeDao;
import recipes.entity.Ingredient;
import recipes.entity.Recipe;
import recipes.entity.Step;
import recipes.entity.Unit;
import recipes.exception.DbException;

public class RecipeService {
	private static final String SCHEMA_FILE = "recipe_schema.sql";
	private static final String DATA_FILE = "recipe_data.sql";
	
	private RecipeDao recipeDao = new RecipeDao();
	
	public Recipe fetchRecipeById(Integer recipeId) {
		return recipeDao.fetchRecipeById(recipeId).orElseThrow(() -> new NoSuchElementException("Recipe with ID=" + recipeId + " does not exist!"));
	}//end METHOD fetchRecipeById
	
	public void createAndPopulateTables() {
		loadFromFile(SCHEMA_FILE);
		loadFromFile(DATA_FILE);
	}//end METHOD createAndPopulateTables

	private void loadFromFile(String fileName) {
		String content = readFilecContent(fileName);
		List<String> sqlStatements = convertContentToSqlStatements(content);
		
//		sqlStatements.forEach(line -> System.out.println(line));
		
		recipeDao.executeBatch(sqlStatements);
	}//end METHOD loadFromFile

	private List<String> convertContentToSqlStatements(String content) {
		content = removeComments(content);
		content = replaceWhitespaceSequencesWithSingleSpace(content);
		
		return extractLinesFromContent(content);
	}//end METHOD convertContentToSqlStatements

	private List<String> extractLinesFromContent(String content) {
		List<String> lines = new LinkedList<>();
		
		while(!content.isEmpty()) {
			int semiColon = content.indexOf(";");
			
			if(semiColon == -1) {
				if(!content.isBlank()) {
					lines.add(content);
				}//end IF content
				
				content = "";
			}//end IF semiColon
			else {
				lines.add(content.substring(0, semiColon).trim());
				content = content.substring(semiColon + 1);
			}//end ELSE
		}//end WHILE
		return lines;
	}//end METHOD extractLinesFromContent

	private String replaceWhitespaceSequencesWithSingleSpace(String content) {
		return content.replaceAll("\\s+", " ");
	}//end METHOD replaceWhitespace

	private String removeComments(String content) {
		StringBuilder builder = new StringBuilder(content);
		int commentPos = 0;
		
		while((commentPos = builder.indexOf("-- ", commentPos)) != -1) {
			int eolPos = builder.indexOf("\n", commentPos + 1);
			
			if(eolPos == -1) {
				builder.replace(commentPos, builder.length(), "");
			}//end IF 
			else {
				builder.replace(commentPos, eolPos + 1, "");
			}//end ELSE
		}//end WHILE
		
		return builder.toString();
	}//end METHOD removeComments

	private String readFilecContent(String fileName) {
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
			return Files.readString(path);
		}//end TRY 
		catch (Exception e) {
			throw new DbException(e);
		}//end CATCH
	}//end METHOD readFilecContent

	public Recipe addRecipe(Recipe recipe) {
		return recipeDao.insertRecipe(recipe);
	}//end METHOD addRecipe

	public List<Recipe> fetchRecipes() {
		return recipeDao.fetchAllRecipes();
	}//end METHOD fetchRecipes

	public List<Unit> fetchUnits() {
		return recipeDao.fetchAllUnits();
	}

	public void addIngredient(Ingredient ingredient) {
		recipeDao.addIngredientToRecipe(ingredient);
	}//end METHOD addIngredient

	public void addStep(Step step) {
		recipeDao.addStepToRecipe(step);
	}//end METHOD addStep

	public List<Category> fetchCategories() {
		return recipeDao.fetchAllCategories();
	}//end METHOD fetchCategories

	public void addCategoryToRecipe(Integer recipeId, String category) {
		recipeDao.addCategoryToRecipe(recipeId, category);
	}//end METHOD addCategoryToRecipe
	
}//end Class