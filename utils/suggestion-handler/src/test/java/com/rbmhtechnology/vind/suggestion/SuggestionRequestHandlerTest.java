package com.rbmhtechnology.vind.suggestion;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rbmhtechnology.vind.suggestion.params.SuggestionRequestParams;

/**
 * http://svn.apache.org/viewvc/lucene/dev/trunk/solr/core/src/test/org/apache/solr/handler/MoreLikeThisHandlerTest.java?view=markup
 *
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class SuggestionRequestHandlerTest extends SolrTestCaseJ4 {

    static private SolrCore core;

    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("solrconfig.xml", "schema.xml", "../../backend/solr/src/main/resources/solrhome", "core");
        core = h.getCore();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        assertU(adoc("_id_", "1",
                "_type_","Asset",
                "dynamic_multi_stored_facet_string_name", "sebastian vettel",
                "dynamic_multi_stored_facet_string_name", "mark webber",
                "dynamic_multi_stored_facet_string_place", "Japan",
                "dynamic_multi_stored_facet_string_place", "Wuppertal",
                "dynamic_multi_stored_facet_string_place", "(1328869589310-619798898)",//for fq test
                "dynamic_multi_stored_facet_string_place", "suzuka",
                "dynamic_single_stored_facet_path_path1", "FIA Formula One World Championship 2012 - Suzuka",
                "dynamic_single_stored_facet_path_path2", "Red Bull Racing Team"));
        assertU(adoc("_id_", "2",
                "_type_","Asset",
                "dynamic_multi_stored_facet_string_name", "sebastian vettel",
                "dynamic_multi_stored_facet_string_name", "Daniel Ricciardo",
                "dynamic_multi_stored_facet_string_place", "Malaysia",
                "dynamic_multi_stored_facet_string_place", "kuala Lumpur",
                "dynamic_single_stored_facet_path_path1", "FIA Formula One World Championship 2012 - kuala Lumpur",
                "dynamic_single_stored_facet_path_path2", "Red Bull Racing Team"));
        assertU(adoc("_id_", "3",
                "_type_","Asset",
                "dynamic_multi_stored_facet_string_name", "stefan Bradl",
                "dynamic_multi_stored_facet_string_place", "Japan",
                "dynamic_multi_stored_facet_string_place", "suzuka",
                "dynamic_multi_stored_facet_string_place", "kuala Lumpur",
                "dynamic_multi_stored_facet_string_brand",  "citroën",
                "dynamic_single_stored_facet_path_path1", "Stefan Bradl - Lifestyle"));
        assertU(adoc("_id_", "4",
                "_type_","Asset",
                "dynamic_multi_stored_facet_string_name", "X-Fighters",
                "dynamic_multi_stored_facet_string_place", "Red Bull"));
        assertU(adoc("_id_", "5",
                "_type_","Asset",
                "dynamic_multi_stored_facet_string_name", "RBXF",
                "dynamic_multi_stored_facet_string_place", "RB"));
        assertU(adoc("_id_", "6",
                "_type_","Asset",
                "dynamic_multi_stored_facet_string_place", "Havanna kuba",
                "dynamic_multi_stored_facet_string_place", "kurdistan"));
        assertU(commit());
    }

    @Test
    public void inputValueEvaluation() {

        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - without query param", req,
                "//response/lst[@name='error']/int[@name='code'][.='400']",
                "//response/lst[@name='error']/str[@name='msg'][.=\"SuggestionRequest needs to have a 'q' parameter\"]");


        params.add(CommonParams.Q, "Sebastian");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - without field params", req,
                "//response/lst[@name='error']/int[@name='code'][.='400']",
                "//response/lst[@name='error']/str[@name='msg'][.=\"SuggestionRequest needs to have at least one 'suggestion.field' parameter or one 'suggestion.multivalue.field' parameter defined.\"]");

        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");

        params.add(SuggestionRequestParams.SUGGESTION_LIMIT,"0");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - with wrong limit",req,
                "//response/lst[@name='error']/int[@name='code'][.='400']",
                "//response/lst[@name='error']/str[@name='msg'][.=\"SuggestionRequest needs to have a 'suggestion.limit' greater than 0\"]");
    }

    @Test
    public void facetSuggestionTest() {

        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"Sebastian");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );
        //params.add(SuggestionRequestParams.SUGGESTION_DF,"spellcheck");

        assertQ("suggester - simple facet suggestion for 'Sebastian'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']");

        params.set(CommonParams.Q, "sebas");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - simple facet suggestion for 'sebas'", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']");

        params.set(CommonParams.Q, "vettel");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - simple facet suggestion for 'vettel'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']");

        params.set(CommonParams.Q,"hans");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - simple facet suggestion for 'hans'", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='0']");

        params.set(CommonParams.Q, "S");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - simple facet suggestion for 'S'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='stefan Bradl'][.='1']");

    }

    @Test
    public void multiFacetSuggestionTest() {

        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"S");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_place");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - simple facet suggestion for 'S'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='3']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='stefan Bradl'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[@name='suzuka'][.='2']");

    }

    @Test
    public void spellcheckSuggestionTest() {

        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"sepastian");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");
        params.add(SuggestionRequestParams.SUGGESTION_DF,"spellcheck");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - spellcheck suggestion for 'sepastian'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']",
                "//response/lst[@name='spellcheck']/lst[@name='collations']/str[@name='collation'][.='sebastian*']");

    }

    @Test
    public void fqParameterTest() {

        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"sebastian");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");
        params.add(CommonParams.FQ,"dynamic_multi_stored_facet_string_place:\"(1328869589310-619798898)\"");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - spellcheck suggestion for 'sepastian'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='1']");

    }

    @Test
    public void testCharacterMapping() {
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"citroën");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_brand");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - character mapping 'Citroën'", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_brand']/int[@name='citroën'][.='1']");

        params.set(CommonParams.Q, "citroen");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - character mapping 'citroen'", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_brand']/int[@name='citroën'][.='1']");

        params.set(CommonParams.Q, "citro");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - character mapping 'citro'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_brand']/int[@name='citroën'][.='1']");
    }

    @Test
    public void testLimitSingleSuggestions() {
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"s");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_place");
        params.add(SuggestionRequestParams.SUGGESTION_LIMIT,"2");
        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - limit test single with result for query 'S'", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[@name='suzuka'][.='2']");

        params.add(SuggestionRequestParams.SUGGESTION_LIMIT_TYPE, SuggestionRequestHandler.LimitType.each.name());
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - limit test single with result for query 'S'", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='3']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='stefan Bradl'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[@name='suzuka'][.='2']");


        params.set(CommonParams.Q, "y");
        req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - limit test single without result for query 'x'",req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='0']");
    }

    @Test
    public void sortingTest() {
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"s");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_place");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - test single sorting for 's' with 2 facets", req,
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[1][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[2][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[1][.='2']");

        //TODO fix
        /*assertQ("suggester - test multi sorting for 's' with 2 facets",req,
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[1]/int[@name='count'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[2]/int[@name='count'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[3]/int[@name='count'][.='1']");
       */
    }

    @Test
    public void sortingTest2() {
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"ku");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_place");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - test single sorting for 'ku' with 2 facets", req,
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[@name='kuala Lumpur'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[1][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[@name='Havanna kuba'][.='1']");
    }

    @Test
    public void testFacetTypesSingle() {
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"s");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_place");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ(req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='3']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='sebastian vettel'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='stefan Bradl'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_place']/int[@name='suzuka'][.='2']");

    }

    @Test
    public void testSynonyms() {
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.add(SuggestionRequestParams.SUGGESTION,"true");
        params.add(CommonParams.Q,"xfighter");
        params.add(SuggestionRequestParams.SUGGESTION_FIELD,"dynamic_multi_stored_facet_string_name");

        SolrQueryRequest req = new LocalSolrQueryRequest( core, params );

        assertQ("suggester - test synonym mapping for single facet", req,
                "//response/lst[@name='suggestions']/int[@name='suggestion_count'][.='2']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='RBXF'][.='1']",
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[@name='dynamic_multi_stored_facet_string_name']/int[@name='X-fighters'][.='1']");

        /* TODO does not work for multifacets
        params.add(SuggestionRequestParams.SUGGESTION_MULTIVALUE,"true");
        params.set(CommonParams.Q,"RB x-fighter");

        assertQ("suggester - test synonym mapping for multi facet",req,
                "//response/lst[@name='suggestions']/lst[@name='suggestion_facets']/lst[1]/int[@name='count'][.='2']");
        */
    }
}
