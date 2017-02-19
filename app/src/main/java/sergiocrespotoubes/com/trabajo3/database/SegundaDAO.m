#import "SegundaDAO.h"
#import "Segunda.h"

@implementation SegundaDAO

static SegundaDAO *instance;

+ (SegundaDAO *) instance {
	if(instance == nil){
		instance = [[SegundaDAO alloc] init];
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

	const char *sql =  "SELECT * FROM Segunda";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Segunda *item = [[Segunda alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.col_fecha = sqlite3_column_int(sqlStatement, 1);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
	}
	return list;
}

- (Segunda *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Segunda where id=%ld",auxid] UTF8String];

	Segunda *item = [[Segunda alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.col_fecha = sqlite3_column_int(sqlStatement, 1);
		}
		sqlite3_finalize(sqlStatement);
	}
	return item;
}

-(void)createObject:(Segunda *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Segunda(COL_FECHA) VALUES ('%ld')", (long)item.col_fecha];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Segunda*)item{


	const char *sql = [[NSString stringWithFormat:@"update Segunda set col_fecha = '%ld' where id=%ld" , item.col_fecha, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Segunda where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end