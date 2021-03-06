package org.gokb.cred

import javax.persistence.Transient
import groovy.util.logging.*
import java.time.LocalDate

@Log4j
class TitleInstancePackagePlatform extends KBComponent {

  LocalDate startDate
  String startVolume
  String startIssue
  String embargo
  RefdataValue coverageDepth
  String coverageNote
  RefdataValue format
  RefdataValue delayedOA
  String delayedOAEmbargo
  RefdataValue hybridOA
  String hybridOAUrl
  RefdataValue primary
  RefdataValue paymentType
  LocalDate endDate
  String endVolume
  String endIssue
  String url
  LocalDate accessStartDate
  LocalDate accessEndDate

  private static refdataDefaults = [
    "format"        : "Electronic",
    "delayedOA"     : "Unknown",
    "hybridOA"      : "Unknown",
    "primary"       : "No",
    "paymentType"   : "Paid",
    "coverageDepth" : "Fulltext"
  ]
  
  static touchOnUpdate = [
    "pkg"
  ]

  static hasByCombo = [
    pkg                 : Package,
    hostPlatform        : Platform,
    title               : TitleInstance,
    derivedFrom         : TitleInstancePackagePlatform,
    masterTipp          : TitleInstancePackagePlatform,
  ]

  static mappedByCombo = [
    pkg                 : 'tipps',
    hostPlatform        : 'hostedTipps',
    additionalPlatforms : 'linkedTipps',
    title               : 'tipps',
    derivatives         : 'derivedFrom'
  ]

  static manyByCombo = [
    derivatives           : TitleInstancePackagePlatform,
    additionalPlatforms   : Platform,
  ]

  static hasMany = [
    coverageStatements : TIPPCoverageStatement
  ]

  static mappedBy = [
    coverageStatements : 'owner'
  ]

  public getPersistentId() {
    "gokb:TIPP:${title?.id}:${pkg?.id}"
  }
  
  public static isTypeCreatable(boolean defaultValue = false) {
    return defaultValue;
  }

  static mapping = {
    includes KBComponent.mapping
    startDate column:'tipp_start_date'
    startVolume column:'tipp_start_volume'
    startIssue column:'tipp_start_issue'
    endDate column:'tipp_end_date'
    endVolume column:'tipp_end_volume'
    endIssue column:'tipp_end_issue'
    embargo column:'tipp_embargo'
    coverageDepth column:'tipp_coverage_depth'
    coverageNote column:'tipp_coverage_note',type: 'text'
    format column:'tipp_format_rv_fk'
    delayedOA column:'tipp_delayed_oa'
    delayedOAEmbargo column:'tipp_delayed_oa_embargo'
    hybridOA column:'tipp_hybrid_oa'
    hybridOAUrl column:'tipp_hybrid_oa_url'
    primary column:'tipp_primary'
    paymentType column:'tipp_payment_type'
    accessStartDate column: 'tipp_access_start_date'
    accessEndDate column: 'tipp_access_end_date'
  }

  static constraints = {
    startDate (nullable:true, blank:true)
    startVolume (nullable:true, blank:true)
    startIssue (nullable:true, blank:true)
    endDate (nullable:true, blank:true)
    endVolume (nullable:true, blank:true)
    endIssue (nullable:true, blank:true)
    embargo (nullable:true, blank:true)
    coverageDepth (nullable:true, blank:true)
    coverageNote (nullable:true, blank:true)
    format (nullable:true, blank:true)
    delayedOA (nullable:true, blank:true)
    delayedOAEmbargo (nullable:true, blank:true)
    hybridOA (nullable:true, blank:true)
    hybridOAUrl (nullable:true, blank:true)
    primary (nullable:true, blank:true)
    paymentType (nullable:true, blank:true)
    accessStartDate (nullable:true, blank:false)
    accessEndDate (nullable:true, blank:false)
  }

  @Transient
  def getPermissableCombos() {
    [
    ]
  }

  public String getNiceName() {
	return "TIPP";
  }

  /**
   * Create a new TIPP being mindful of the need to create TIPLs
   */
  public static tiplAwareCreate(tipp_fields = [:]) {

    def result = new TitleInstancePackagePlatform(tipp_fields)
    result.title = tipp_fields.title
    result.hostPlatform = tipp_fields.hostPlatform
    result.pkg = tipp_fields.pkg
    
    // See if there is a TIPL
    TitleInstancePlatform.ensure(tipp_fields.title, tipp_fields.hostPlatform, tipp_fields.url);

    result
  }

  @Override
  @Transient
  public String getDisplayName() {
    return name ?: "${pkg?.name} / ${title?.name} / ${hostPlatform?.name}"
  }

  /**
   * Please see https://github.com/k-int/gokb-phase1/wiki/tipp_dto
   */ 
  @Transient
  public static boolean validateDTO(tipp_dto) {
    def result = true;
    result &= tipp_dto.package?.internalId != null
    result &= tipp_dto.platform?.internalId != null
    result &= tipp_dto.title?.internalId != null

    if ( !result ) 
      log.warn("Tipp failed validation: ${tipp_dto} - pkg:${tipp_dto.package?.internalId} plat:${tipp_dto.platform?.internalId} ti:${tipp_dto.title?.internalId}");

    result;
  }

  /**
   * Please see https://github.com/k-int/gokb-phase1/wiki/tipp_dto
   */ 
  @Transient
  public static TitleInstancePackagePlatform upsertDTO(tipp_dto) {
    def result = null;
    log.debug("upsertDTO(${tipp_dto})");
    def pkg = Package.get(tipp_dto.package?.internalId)
    def plt = Platform.get(tipp_dto.platform?.internalId)
    def ti = TitleInstance.get(tipp_dto.title?.internalId)

    if ( pkg && plt && ti ) {
      log.debug("See if we already have a tipp");
      def tipps = TitleInstance.executeQuery('select tipp.id from TitleInstancePackagePlatform as tipp, Combo as pkg_combo, Combo as title_combo, Combo as platform_combo  '+
                                           'where pkg_combo.toComponent=tipp and pkg_combo.fromComponent=?'+
                                           'and platform_combo.toComponent=tipp and platform_combo.fromComponent = ?'+
                                           'and title_combo.toComponent=tipp and title_combo.fromComponent = ?',
                                          [pkg,plt,ti])
      def tipp = null;
      switch ( tipps.size() ) {
        case 1:
          log.debug("found");
          tipp = TitleInstancePackagePlatform.get(tipps[0])
          break;
        case 0:
          log.debug("not found");
          tipp=new TitleInstancePackagePlatform()
          tipp.pkg = pkg;
          tipp.title = ti;
          tipp.hostPlatform = plt;
          break;
        default:
          log.error("Multiple matches found for tipp..");
          break;
      }

      if ( tipp ) {
        tipp.save(flush:true,failOnError:true);
        def changed = false

        changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'url', tipp_dto.url)

        tipp_dto.coverage.each { c ->
          changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'startVolume', c.startVolume)
          changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'startVolume', c.startIssue)
          changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'endVolume', c.endVolume)
          changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'endVolume', c.endIssue)
          changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'embargo', c.embargo)
          changed |= com.k_int.ClassUtils.setStringIfDifferent(tipp, 'coverageNote', c.coverageNote)
          changed |= com.k_int.ClassUtils.setDateIfPresent(c.startDate,tipp,'startDate')
          changed |= com.k_int.ClassUtils.setDateIfPresent(c.endDate,tipp,'endDate')
          // refdata setStringIfDifferent(tipp, 'coverageDepth', c.coverageDepth)
        }

        tipp.save(flush:true, failOnError:true);
      }
      result = tipp;
    }

    result;
  }

}
