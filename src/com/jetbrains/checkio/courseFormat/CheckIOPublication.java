package com.jetbrains.checkio.courseFormat;

import org.jetbrains.annotations.NotNull;

public class CheckIOPublication {
  public static final String PUBLICATION_PREFIX = "solutionBy";
  public CheckIOUser myAuthor;
  public String myText;
  public CheckIOPublicationCategory myCategory;
  public String runner;

  public CheckIOPublication(@NotNull final CheckIOUser author,
                            @NotNull final String text,
                            @NotNull final CheckIOPublicationCategory category,
                            @NotNull final String sdk) {
    myAuthor = author;
    myText = text;
    myCategory = category;
    runner = sdk;
  }

  public String getPublicationName() {
    return PUBLICATION_PREFIX + myAuthor.getUsername();
  }

  public String getPublicationFileNameWithExtension() {
    return getPublicationName() + ".py";
  }


  @Override
  public String toString() {
    return getPublicationName();
  }
}
