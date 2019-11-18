package edu.stanford.biosearch.model.integrateddata;

import edu.stanford.biosearch.model.integrateddata.filter.Filter;
import java.util.LinkedList;
import java.util.List;

/*
 * Elastic Boolean Query
 *
 * */
public class Bool {
  /*
   * TODO:Support Occurrences in a more generic way?
   *
   * must
   * filter
   * should
   * must_not
   *
   * */

  private List<Filter> must = new LinkedList<Filter>();
  // private List<Match> must_not = new LinkedList<Match>();
  // private List<Match> should = new LinkedList<Match>();

  public List<Filter> getMust() {
    return this.must;
  }

}
