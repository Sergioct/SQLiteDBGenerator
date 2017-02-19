#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "Primera.h"

@interface PrimeraDAO : NSObject{
	sqlite3 *db;
}

+ (PrimeraDAO *) instance;

- (void) createObject:(Primera*) item;

- (NSMutableArray *) getAll;

- (Primera *) getById:(NSInteger)auxid;

- (void) updateObject:(Primera*) item;

- (void) deleteObject:(NSInteger)auxid;

@end