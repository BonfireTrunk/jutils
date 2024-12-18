package today.bonfire.jutils.constants;

public enum ContinentCodes {
  AF("Africa"),
  AN("Antarctica"),
  AS("Asia"),
  EU("Europe"),
  NA("North America"),
  OC("Oceania"),
  SA("South America"),
  XX("Unknown") // dummy placeholder
  ;

  private final String name;

  ContinentCodes(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }

}
