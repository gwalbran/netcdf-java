/*
 * Copyright (c) 1998-2020 John Caron and University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package ucar.nc2.dataset;

import com.google.common.collect.ImmutableList;
import java.util.Formatter;
import ucar.nc2.AttributeContainer;
import ucar.nc2.AttributeContainerMutable;
import ucar.unidata.util.Parameter;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * A CoordinateTransform is an abstraction of a function from a CoordinateSystem to a
 * "reference" CoordinateSystem.
 *
 * CoordinateTransform is the superclass for ProjectionCT and VerticalCT.
 * It contains the Attributes/Parameters needed to make a "Coordinate Transform Variable" which
 * is just a container for the Transform parameters.
 * LOOK this should be abstract.
 */
@ThreadSafe
public class CoordinateTransform implements Comparable<CoordinateTransform> {
  /**
   * Create a Coordinate Transform.
   *
   * @param name name of transform, must be unique within the Coordinate System.
   * @param authority naming authority
   * @param transformType type of transform.
   * @param params list of Parameters.
   */
  protected CoordinateTransform(String name, String authority, TransformType transformType, List<Parameter> params) {
    this.name = name;
    this.authority = authority;
    this.transformType = transformType;
    this.params = ImmutableList.copyOf(params);
  }

  /**
   * Create a Coordinate Transform.
   *
   * @param name name of transform, must be unique within the NcML.
   * @param authority naming authority
   * @param transformType type of transform.
   * @deprecated Use CoordinateTransform.builder()
   */
  @Deprecated
  public CoordinateTransform(String name, String authority, TransformType transformType) {
    this.name = name;
    this.authority = authority;
    this.transformType = transformType;
    this.params = new ArrayList<>();
  }

  /**
   * add a parameter
   * 
   * @param param add this Parameter
   * @deprecated Use CoordinateTransform.builder()
   */
  @Deprecated
  public void addParameter(Parameter param) {
    params.add(param);
  }

  public String getName() {
    return name;
  }

  public AttributeContainer getAttributeContainer() {
    return attributeContainer;
  }

  public String getAuthority() {
    return authority;
  }

  public TransformType getTransformType() {
    return transformType;
  }

  public ImmutableList<Parameter> getParameters() {
    return ImmutableList.copyOf(params);
  }

  /**
   * Convenience function; look up Parameter by name, ignoring case.
   *
   * @param name the name of the attribute
   * @return the Attribute, or null if not found
   */
  public Parameter findParameterIgnoreCase(String name) {
    for (Parameter a : params) {
      if (name.equalsIgnoreCase(a.getName()))
        return a;
    }
    return null;
  }

  /**
   * Instances which have same name, authority and parameters are equal.
   */
  public boolean equals(Object oo) {
    if (this == oo)
      return true;
    if (!(oo instanceof CoordinateTransform))
      return false;

    CoordinateTransform o = (CoordinateTransform) oo;
    if (!getName().equals(o.getName()))
      return false;
    if (!getAuthority().equals(o.getAuthority()))
      return false;
    if (!(getTransformType() == o.getTransformType()))
      return false;

    List<Parameter> oparams = o.getParameters();
    if (params.size() != oparams.size())
      return false;

    for (int i = 0; i < params.size(); i++) {
      Parameter att = params.get(i);
      Parameter oatt = oparams.get(i);
      if (!att.getName().equals(oatt.getName()))
        return false;
      // if (!att.getValue().equals(oatt.getValue())) return false;
    }
    return true;
  }

  /**
   * Override Object.hashCode() to be consistent with equals.
   */
  public int hashCode() {
    if (hashCode == 0) {
      int result = 17;
      result = 37 * result + getName().hashCode();
      result = 37 * result + getAuthority().hashCode();
      result = 37 * result + getTransformType().hashCode();
      for (Parameter att : params) {
        result = 37 * result + att.getName().hashCode();
        // result = 37*result + att.getValue().hashCode();
      }
      hashCode = result;
    }
    return hashCode;
  }

  private volatile int hashCode;

  public String toString() {
    return name;
  }

  @Override
  public int compareTo(CoordinateTransform oct) {
    return name.compareTo(oct.getName());
  }

  ////////////////////////////////////////////////////////////////////////////////////////////
  // TODO make these final and immutable in 6.

  protected String name, authority;
  protected final TransformType transformType;
  protected List<Parameter> params;
  private AttributeContainerMutable attributeContainer;

  // LOOK this is wrong, should create a ProjectionCT or a VerticalCT.
  protected CoordinateTransform(Builder<?> builder, NetcdfDataset ncd) {
    this.name = builder.name;
    this.authority = builder.authority;
    this.transformType = builder.transformType;
    this.attributeContainer = new AttributeContainerMutable(this.name);
    this.attributeContainer.addAll(builder.attributeContainer);

    CoordinateTransform ct =
        CoordTransBuilder.makeCoordinateTransform(ncd, builder.attributeContainer, new Formatter(), new Formatter());
    ct.attributeContainer = new AttributeContainerMutable(this.name);
    ct.attributeContainer.addAll(builder.attributeContainer);
  }

  public Builder<?> toBuilder() {
    return addLocalFieldsToBuilder(builder());
  }

  // Add local fields to the passed - in builder.
  protected Builder<?> addLocalFieldsToBuilder(Builder<? extends Builder<?>> b) {
    return b.setName(this.name).setAuthority(this.authority).setTransformType(this.transformType)
        .setAttributeContainer(this.attributeContainer);
  }

  /**
   * Get Builder for this class that allows subclassing.
   * 
   * @see "https://community.oracle.com/blogs/emcmanus/2010/10/24/using-builder-pattern-subclasses"
   */
  public static Builder<?> builder() {
    return new Builder2();
  }

  private static class Builder2 extends Builder<Builder2> {
    @Override
    protected Builder2 self() {
      return this;
    }
  }

  public static abstract class Builder<T extends Builder<T>> {
    public String name;
    private String authority;
    private TransformType transformType;
    private AttributeContainer attributeContainer;
    private CoordinateTransform preBuilt;
    private boolean built;

    protected abstract T self();

    public T setName(String name) {
      this.name = name;
      return self();
    }

    public T setAuthority(String authority) {
      this.authority = authority;
      return self();
    }

    public T setTransformType(TransformType transformType) {
      this.transformType = transformType;
      return self();
    }

    public T setAttributeContainer(AttributeContainer attributeContainer) {
      this.attributeContainer = attributeContainer;
      return self();
    }

    public T setPreBuilt(CoordinateTransform preBuilt) {
      this.preBuilt = preBuilt;
      this.name = preBuilt.name;
      return self();
    }

    public CoordinateTransform build(NetcdfDataset ncd) {
      return build(ncd, ncd.getCoordinateAxes());
    }

    public CoordinateTransform build(NetcdfDataset ncd, ImmutableList<CoordinateAxis> coordAxes) {
      if (built)
        throw new IllegalStateException("already built " + name);
      built = true;

      if (this.preBuilt != null) {
        return this.preBuilt;
      }

      // All this trouble because we need ncd before we can build.
      CoordinateTransform ct = CoordTransBuilder.makeCoordinateTransform(ncd, attributeContainer, new Formatter(),
          new Formatter(), coordAxes);
      if (ct != null) {
        // ct.name = this.name; // LOOK why is this commented out? Dont know name until this point? Not going to
        // work....
        ct.attributeContainer = new AttributeContainerMutable(this.name);
        ct.attributeContainer.addAll(attributeContainer);
      }

      return ct;
    }
  }

}
