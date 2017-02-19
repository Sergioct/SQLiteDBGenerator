#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "Primera.h"

@interface PrimeraDAO : NSObject{
	sqlite3 *db;
}

+ (Primera *) instance;

- (void) createObject:(Primera*) item;

- (NSMutableArray *) getAll;

- (NSMutableArray *) getById:(NSInteger)auxid;

- (void) updateObject:(Primera*) item;

- (void) deleteObject:(NSInteger)auxid;

@end