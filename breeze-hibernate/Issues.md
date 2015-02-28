## hibernate issues:

   1.  Not sure how to represent/map Guids in hibernate, problem with strings is case sensitivity.  Guids are not case sensitive, strings are. But MySql and other databases don't have a Guid datatype. It may be enough to simply change the metadata published from hibernate.
   2.  Byte[] vs Blob ( see Category.Picture - don't know how to map properly)

