package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Selector extends ASTCssNode implements Cloneable {

  private List<SelectorPart> combinedParts = new ArrayList<SelectorPart>();

  public Selector(HiddenTokenAwareTree token) {
    super(token);
  }

  public Selector(HiddenTokenAwareTree token, SelectorPart head) {
    this(token, ArraysUtils.asModifiableList(head));
  }

  public Selector(HiddenTokenAwareTree token, List<SelectorPart> combinedParts) {
    super(token);
    this.combinedParts = combinedParts;
  }

  public List<SelectorPart> getParts() {
    return combinedParts;
  }

  public void addPart(SelectorPart part) {
    combinedParts.add(part);
  }

  public void addParts(List<SelectorPart> parts) {
    combinedParts.addAll(parts);
  }

  public void removeHead() {
    combinedParts.remove(0);
  }

  /* *********************************************************** */
  //FIXME (!!!!!!-selector refactoring)
  public boolean isExtending() {
    if (hasRight())
      return findLastPart().isExtending();

    return getHead().isExtending();
  }

  //FIXME (!!!- semi - selector refactoring)
  public SelectorPart getHead() {
    List<SelectorPart> parts = getParts();
    if (parts.isEmpty())
      return null;

    return parts.get(0);
  }

  //FIXME (!!!!!!-selector refactoring)
  public void setHead(SelectorPart head) {
    if (!combinedParts.isEmpty())
      combinedParts.remove(0);

    combinedParts.add(0, head);
  }

  //FIXME (!!!!!!-selector refactoring)
  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return new ArrayList<ASTCssNode>(combinedParts);
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR;
  }

  @Override
  public Selector clone() {
    Selector clone = (Selector) super.clone();
    clone.combinedParts = ArraysUtils.deeplyClonedList(combinedParts);
    clone.configureParentToAllChilds();

    return clone;
  }

  //FIXME (!!!!!!-selector refactoring)
  public SelectorPart findLastPart() {
    return ArraysUtils.last(getParts());
  }

  //FIXME (!!!!!!-selector refactoring)
  public boolean hasRight() {
    return isCombined();
  }

  public boolean isCombined() {
    return combinedParts.size() > 1;
  }

  //FIXME (!!!!!!-selector refactoring)
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Selector [");
    builder.append(combinedParts);
    builder.append("]");
    return builder.toString();
  }

  public NestedSelectorAppender findFirstAppender() {
    for (SelectorPart part : getParts()) {
      if (part.isAppender())
        return (NestedSelectorAppender) part;
    }
    
    return null;
  }

  public SelectorPart findFirstNonAppender() {
    for (SelectorPart part : getParts()) {
      if (!part.isAppender())
        return part;
    }
    
    return null;
  }

  public boolean containsAppender() {
    return findFirstAppender() != null;
  }

  //FIXME (!!!!!!-selector refactoring)
  public boolean isReusableSelector() {
    Iterator<SelectorPart> parts = getParts().iterator();
    SelectorPart current = parts.next();
    // skip initial appernders
    while (current.isAppender() && parts.hasNext()) {
      current = parts.next();
    }

    // find out whether there is something not reausable 
    while (current.isClassesAndIdsOnlySelector() && parts.hasNext()) {
      current = parts.next();
    }

    return current.isClassesAndIdsOnlySelector();
  }

  /**
   * Assumes that hasReusableHead returns true
   * 
   * @return
   */
  public ReusableStructureName toReusableStructureName() {
    List<ElementSubsequent> nameParts = extractReusableNameParts();

    ReusableStructureName result = new ReusableStructureName(nameParts.get(0).getUnderlyingStructure(), nameParts);
    return result;
  }

  // We are loosing a lot of information during the extraction. It is ok, 
  // because less.js is not using combinators and does not distinguish 
  // between .aaa.bbb and .aaa .bbb
  private List<ElementSubsequent> extractReusableNameParts() {
    List<ElementSubsequent> result = new ArrayList<ElementSubsequent>();
    for (SelectorPart part : getParts()) {
      if (!part.isAppender()) {
        result.addAll(((SimpleSelector) part).getSubsequent());
      }
    }
    return result;
  }

  //FIXME: (!!!!!) remove
  @Deprecated
  public void setLeadingCombinator(SelectorCombinator combinator) {
    getHead().setLeadingCombinator(combinator);
  }

  //FIXME: (!!!!!) remove
  @Deprecated
  public SelectorCombinator getLeadingCombinator() {
    return getHead().getLeadingCombinator();
  }

  //FIXME: (!!!!!) remove
  @Deprecated
  public boolean hasLeadingCombinator() {
    return getHead().hasLeadingCombinator();
  }

  //FIXME: (!!!!!) remove
  @NotAstProperty
  public List<SelectorPart> getAllExceptHead() {
    List<SelectorPart> result = new ArrayList<SelectorPart>(getParts());
    result.remove(0);
    return result;
  }

  //FIXME: (!!!!!) remove
  public SelectorPart findSecondPart() {
    return combinedParts.get(1);
  }

}
