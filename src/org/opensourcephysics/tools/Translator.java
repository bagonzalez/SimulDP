package org.opensourcephysics.tools;

public interface Translator {

	/**
	 * Gets the localized value of a property for the specified class.
	 * If no localized value is found, the key is returned.
	 *
	 * @param type the class requesting the localized value
	 * @param key the string to localize
	 * @return the localized string
	 */
	public String getProperty(Class<?> type, String key);

	/**
	 * Gets the localized value of a property for the specified class.
	 * If no localized value is found, the defaultValue is returned.
	 *
	 * @param type the class requesting the localized value
	 * @param key the string to localize
	 * @param defaultValue the default if no localized value found
	 * @return the localized string
	 */
	public String getProperty(Class<?> type, String key, String defaultValue);

  /**
   * Gets the localized value of a property for the specified object.
   * The object must first be associated with a class.
   * If no localized value is found, the key is returned.
   *
   * @param obj the object requesting the localized value
   * @param key the string to localize
   * @return the localized string
   */
  public String getProperty(Object obj, String key);

  /**
   * Gets the localized value of a property for the specified object.
   * The object must first be associated with a class.
   * If no localized value is found, the defaultValue is returned.
   *
   * @param obj the object requesting the localized value
   * @param key the string to localize
   * @param defaultValue the default if no localized value found
   * @return the localized string
   */
  public String getProperty(Object obj, String key, String defaultValue);

  /**
   * Associates an object with a class for property lookup purposes.
   *
   * @param obj the object needing translations
   * @param type the class
   */
  public void associate(Object obj, Class<?> type);

  /**
   * Shows the properties for the specified class.
   *
   * @param type the class
   */
  public void showProperties(Class<?> type);

  /**
	 * Sets the visibility.
	 *
	 * @param visible true to set this visible
	 */
	public void setVisible(boolean visible);

}
