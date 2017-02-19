#import "PrimeraDAO.h"
#import "Primera.h"

@implementation PrimeraDAO

static PrimeraDAO *instance;

+ (PrimeraDAO *) instance {
	if(instance == nil){
		instance = [[PrimeraDAO alloc] init];
	}
	return instance;
}

- (id)init {
	if ((self = [super init])) {
		NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString* documentsDirectory = [paths lastObject];
		NSString* databasePath = [documentsDirectory stringByAppendingPathComponent:@"database.sqlite3"];
		if (sqlite3_open([sqLiteDb UTF8String], &db) != SQLITE_OK) {
			NSLog(@"Failed to open database!");
		}
	}
	return self;
}

- (NSMutableArray *) getAll{

	const char *sql =  "SELECT * FROM Primera";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Primera *item = [[Primera alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.col_primaria = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.col_unica = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 2)];
			item.col_entero = sqlite3_column_int(sqlStatement, 3);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
	}
	return list;
}

- (Primera *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Primera where id=%ld",auxid] UTF8String];

	Primera *item = [[Primera alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.col_primaria = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.col_unica = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 2)];
			item.col_entero = sqlite3_column_int(sqlStatement, 3);
		}
		sqlite3_finalize(sqlStatement);
	}
	return item;
}

-(void)createObject:(Primera *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Primera(COL_PRIMARIA, COL_UNICA, COL_ENTERO) VALUES ('%@', '%@', '%ld')", item.col_primaria, item.col_unica, (long)item.col_entero];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Primera*)item{


	const char *sql = [[NSString stringWithFormat:@"update Primera set col_primaria = '%@', col_unica = '%@', col_entero = '%ld' where id=%ld" , item.col_primaria, item.col_unica, item.col_entero, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Primera where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end